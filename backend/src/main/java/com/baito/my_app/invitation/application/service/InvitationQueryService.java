package com.baito.my_app.invitation.application.service;

import com.baito.my_app.common.domain.PageResult;
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

    public InvitationQueryService(InvitationRepository invitationRepository,
                                  MemberRepository memberRepository) {
        this.invitationRepository = invitationRepository;
        this.memberRepository = memberRepository;
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
    public List<Invitation> getReceivedPendingInvitations(Long inviteeId) {
        return invitationRepository.findPendingByInviteeId(inviteeId);
    }
}
