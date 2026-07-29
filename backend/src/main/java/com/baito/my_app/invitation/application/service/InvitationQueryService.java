package com.baito.my_app.invitation.application.service;

import com.baito.my_app.common.domain.PageResult;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.invitation.application.port.in.GetInvitationsQuery;
import com.baito.my_app.invitation.application.port.out.InvitationRepository;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationStatus;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InvitationQueryService implements GetInvitationsQuery {

    private final InvitationRepository invitationRepository;
    private final MemberRepository memberRepository;
    private final WorkGroupRepository workGroupRepository;

    public InvitationQueryService(InvitationRepository invitationRepository,
                                  MemberRepository memberRepository,
                                  WorkGroupRepository workGroupRepository) {
        this.invitationRepository = invitationRepository;
        this.memberRepository = memberRepository;
        this.workGroupRepository = workGroupRepository;
    }

    @Override
    public PageResult<SentInvitation> getSentInvitations(Long inviterId, Long groupId, InvitationStatus status,
                                                         int page, int size) {
        PageResult<Invitation> invitations =
                invitationRepository.findByInviterIdAndGroupId(inviterId, groupId, status, page, size);

        Map<Long, Member> inviteesById = memberRepository.findAllByIds(
                        invitations.content().stream().map(Invitation::getInviteeId).distinct().toList()).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        return invitations.map(inv -> {
            Member invitee = inviteesById.get(inv.getInviteeId());
            return new SentInvitation(inv,
                    invitee != null ? invitee.getName() : null,
                    invitee != null ? invitee.getLoginId() : null);
        });
    }

    @Override
    public List<ReceivedInvitation> getReceivedPendingInvitations(Long inviteeId) {
        List<Invitation> invitations = invitationRepository.findPendingByInviteeId(inviteeId);

        Map<Long, WorkGroup> groupsById = workGroupRepository.findAllByIds(
                        invitations.stream().map(Invitation::getGroupId).distinct().toList()).stream()
                .collect(Collectors.toMap(WorkGroup::getId, Function.identity()));
        Map<Long, Member> invitersById = memberRepository.findAllByIds(
                        invitations.stream().map(Invitation::getInviterId).distinct().toList()).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        return invitations.stream().map(inv -> {
            WorkGroup group = groupsById.get(inv.getGroupId());
            Member inviter = invitersById.get(inv.getInviterId());
            return new ReceivedInvitation(inv,
                    group != null ? group.getName() : null,
                    inviter != null ? inviter.getName() : null);
        }).toList();
    }
}
