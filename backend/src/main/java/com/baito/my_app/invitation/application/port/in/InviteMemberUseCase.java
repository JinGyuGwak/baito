package com.baito.my_app.invitation.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public interface InviteMemberUseCase {

    /**
     * Owner invites a part-timer to a group, looked up by the invitee's login ID.
     *
     * @return the created invitation id
     */
    Long invite(Command command);

    @Getter
    @Setter
    @AllArgsConstructor
    class Command {
        private Long groupId;
        private Long inviterId;
        private String inviteeLoginId;
    }
}
