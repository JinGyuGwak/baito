package com.baito.my_app.invitation.application.port.in;

import com.baito.my_app.common.domain.PageResult;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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

    /**
     * Pending invitations received by a part-timer, enriched with the group's name and the
     * inviting owner's name for display.
     */
    List<ReceivedInvitation> getReceivedPendingInvitations(Long inviteeId);

    /** A sent invitation joined with the invitee's identity for display. */
    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode
    class SentInvitation {
        private Invitation invitation;
        private String inviteeName;
        private String inviteeLoginId;
    }

    /** A received invitation joined with the group name and inviting owner's name for display. */
    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode
    class ReceivedInvitation {
        private Invitation invitation;
        private String groupName;
        private String inviterName;
    }
}
