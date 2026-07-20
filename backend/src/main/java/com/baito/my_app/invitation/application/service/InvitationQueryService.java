package com.baito.my_app.invitation.application.service;

import com.baito.my_app.invitation.application.port.in.GetInvitationsQuery;
import com.baito.my_app.invitation.application.port.out.InvitationRepository;
import com.baito.my_app.invitation.domain.Invitation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class InvitationQueryService implements GetInvitationsQuery {

    private final InvitationRepository invitationRepository;

    public InvitationQueryService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    public List<Invitation> getSentInvitations(Long inviterId) {
        return invitationRepository.findByInviterId(inviterId);
    }

    @Override
    public List<Invitation> getReceivedPendingInvitations(Long inviteeId) {
        return invitationRepository.findPendingByInviteeId(inviteeId);
    }
}
