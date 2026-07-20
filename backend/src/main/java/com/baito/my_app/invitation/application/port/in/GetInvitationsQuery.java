package com.baito.my_app.invitation.application.port.in;

import com.baito.my_app.invitation.domain.Invitation;

import java.util.List;

public interface GetInvitationsQuery {

    /** All invitations sent by an owner. */
    List<Invitation> getSentInvitations(Long inviterId);

    /** Pending invitations received by a part-timer. */
    List<Invitation> getReceivedPendingInvitations(Long inviteeId);
}
