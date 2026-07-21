package com.baito.my_app.invitation.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public interface CancelInvitationUseCase {

    /**
     * Owner cancels a PENDING invitation they sent.
     */
    void cancel(Command command);

    @Getter
    @Setter
    @AllArgsConstructor
    class Command {
        private Long invitationId;
        private Long requesterId;
    }
}
