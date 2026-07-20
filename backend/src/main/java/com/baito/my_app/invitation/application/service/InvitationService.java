package com.baito.my_app.invitation.application.service;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.invitation.application.port.in.CancelInvitationUseCase;
import com.baito.my_app.invitation.application.port.in.InviteMemberUseCase;
import com.baito.my_app.invitation.application.port.in.RespondInvitationUseCase;
import com.baito.my_app.invitation.application.port.out.InvitationRepository;
import com.baito.my_app.invitation.domain.DuplicatePendingInvitationException;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationAccessDeniedException;
import com.baito.my_app.invitation.domain.InvalidInviteeException;
import com.baito.my_app.invitation.domain.InvitationNotFoundException;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class InvitationService implements InviteMemberUseCase, CancelInvitationUseCase, RespondInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final WorkGroupRepository workGroupRepository;
    private final MemberRepository memberRepository;
    private final GroupMembershipRepository membershipRepository;

    public InvitationService(InvitationRepository invitationRepository,
                             WorkGroupRepository workGroupRepository,
                             MemberRepository memberRepository,
                             GroupMembershipRepository membershipRepository) {
        this.invitationRepository = invitationRepository;
        this.workGroupRepository = workGroupRepository;
        this.memberRepository = memberRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    public Long invite(InviteMemberUseCase.Command command) {
        WorkGroup group = workGroupRepository.findById(command.groupId())
                .orElseThrow(() -> new NotGroupOwnerException(command.groupId()));
        if (!group.isOwnedBy(command.inviterId())) {
            throw new NotGroupOwnerException(command.groupId());
        }

        Member invitee = memberRepository.findByLoginId(command.inviteeLoginId())
                .orElseThrow(() -> new InvalidInviteeException(
                        "존재하지 않는 로그인 ID입니다: " + command.inviteeLoginId()));
        if (!invitee.isPartTimer()) {
            throw new InvalidInviteeException("아르바이트생만 초대할 수 있습니다: " + command.inviteeLoginId());
        }
        if (membershipRepository.existsActiveMembership(command.groupId(), invitee.id())) {
            throw new InvalidInviteeException("이미 그룹에 소속된 아르바이트생입니다: " + command.inviteeLoginId());
        }
        if (invitationRepository.existsPendingByGroupIdAndInviteeId(command.groupId(), invitee.id())) {
            throw new DuplicatePendingInvitationException();
        }

        Invitation invitation = Invitation.create(command.groupId(), command.inviterId(), invitee.id());
        return invitationRepository.save(invitation).id();
    }

    @Override
    public void cancel(CancelInvitationUseCase.Command command) {
        Invitation invitation = loadInvitation(command.invitationId());
        if (!invitation.inviterId().equals(command.requesterId())) {
            throw new InvitationAccessDeniedException();
        }
        invitationRepository.save(invitation.cancel(LocalDateTime.now()));
    }

    @Override
    public void accept(RespondInvitationUseCase.Command command) {
        Invitation invitation = loadInvitationForInvitee(command);
        LocalDateTime now = LocalDateTime.now();
        invitationRepository.save(invitation.accept(now));
        activateMembership(invitation.groupId(), invitation.inviteeId(), now);
    }

    @Override
    public void reject(RespondInvitationUseCase.Command command) {
        Invitation invitation = loadInvitationForInvitee(command);
        invitationRepository.save(invitation.reject(LocalDateTime.now()));
    }

    private Invitation loadInvitation(Long invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException(invitationId));
    }

    private Invitation loadInvitationForInvitee(RespondInvitationUseCase.Command command) {
        Invitation invitation = loadInvitation(command.invitationId());
        if (!invitation.inviteeId().equals(command.requesterId())) {
            throw new InvitationAccessDeniedException();
        }
        return invitation;
    }

    private void activateMembership(Long groupId, Long memberId, LocalDateTime now) {
        // Idempotent guard against the (group_id, member_id) unique constraint.
        if (membershipRepository.findByGroupIdAndMemberId(groupId, memberId).isEmpty()) {
            membershipRepository.save(GroupMembership.activate(groupId, memberId, now));
        }
    }
}
