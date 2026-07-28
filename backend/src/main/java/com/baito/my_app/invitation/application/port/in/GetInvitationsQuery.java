package com.baito.my_app.invitation.application.port.in;

import com.baito.my_app.common.domain.PageResult;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationStatus;

import java.util.List;

public interface GetInvitationsQuery {

    /**
     * Invitations an owner has sent for one group, newest first, enriched with the invitee's
     * name and loginId.
     *
     * @param status optional status filter; {@code null} returns every status
     */
    PageResult<SentInvitation> getSentInvitations(Long inviterId, Long groupId, InvitationStatus status,
                                                  int page, int size);

    /** Pending invitations received by a part-timer. */
    List<Invitation> getReceivedPendingInvitations(Long inviteeId);

    /** A sent invitation joined with the invitee's identity for display. */
    record SentInvitation(Invitation invitation, String inviteeName, String inviteeLoginId) {
    }
}
