package com.baito.my_app.invitation.application.port.in;

public interface InviteMemberUseCase {

    /**
     * Owner invites a part-timer to a group, looked up by the invitee's login ID.
     *
     * @return the created invitation id
     */
    Long invite(Command command);

    record Command(Long groupId, Long inviterId, String inviteeLoginId) {
    }
}
