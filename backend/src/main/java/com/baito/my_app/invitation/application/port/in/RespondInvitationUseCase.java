package com.baito.my_app.invitation.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public interface RespondInvitationUseCase {

    /**
     * Part-timer accepts a PENDING invitation. A GroupMembership is created (activated).
     */
    void accept(Command command);

    /**
     * Part-timer rejects a PENDING invitation.
     */
    void reject(Command command);

    @Getter
    @Setter
    @AllArgsConstructor
    class Command {
        private Long invitationId;
        private Long requesterId;
    }
}
