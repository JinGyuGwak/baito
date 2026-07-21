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
        WorkGroup group = workGroupRepository.findById(command.getGroupId())
                .orElseThrow(() -> new NotGroupOwnerException(command.getGroupId()));
        if (!group.isOwnedBy(command.getInviterId())) {
            throw new NotGroupOwnerException(command.getGroupId());
        }

        Member invitee = memberRepository.findByLoginId(command.getInviteeLoginId())
                .orElseThrow(() -> new InvalidInviteeException(
                        "존재하지 않는 로그인 ID입니다: " + command.getInviteeLoginId()));
        if (!invitee.isPartTimer()) {
            throw new InvalidInviteeException("아르바이트생만 초대할 수 있습니다: " + command.getInviteeLoginId());
        }
        if (membershipRepository.existsActiveMembership(command.getGroupId(), invitee.getId())) {
            throw new InvalidInviteeException("이미 그룹에 소속된 아르바이트생입니다: " + command.getInviteeLoginId());
        }
        if (invitationRepository.existsPendingByGroupIdAndInviteeId(command.getGroupId(), invitee.getId())) {
            throw new DuplicatePendingInvitationException();
        }

        Invitation invitation = Invitation.create(command.getGroupId(), command.getInviterId(), invitee.getId());
        return invitationRepository.save(invitation).getId();
    }

    @Override
    public void cancel(CancelInvitationUseCase.Command command) {
        Invitation invitation = loadInvitation(command.getInvitationId());
        if (!invitation.getInviterId().equals(command.getRequesterId())) {
            throw new InvitationAccessDeniedException();
        }
        invitation.cancel(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    @Override
    public void accept(RespondInvitationUseCase.Command command) {
        Invitation invitation = loadInvitationForInvitee(command);
        LocalDateTime now = LocalDateTime.now();
        invitation.accept(now);
        invitationRepository.save(invitation);
        activateMembership(invitation.getGroupId(), invitation.getInviteeId(), now);
    }

    @Override
    public void reject(RespondInvitationUseCase.Command command) {
        Invitation invitation = loadInvitationForInvitee(command);
        invitation.reject(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    private Invitation loadInvitation(Long invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException(invitationId));
    }

    private Invitation loadInvitationForInvitee(RespondInvitationUseCase.Command command) {
        Invitation invitation = loadInvitation(command.getInvitationId());
        if (!invitation.getInviteeId().equals(command.getRequesterId())) {
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
