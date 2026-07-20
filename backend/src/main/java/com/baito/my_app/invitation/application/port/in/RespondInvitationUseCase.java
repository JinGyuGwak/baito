package com.baito.my_app.invitation.application.port.in;

public interface RespondInvitationUseCase {

    /**
     * Part-timer accepts a PENDING invitation. A GroupMembership is created (activated).
     */
    void accept(Command command);

    /**
     * Part-timer rejects a PENDING invitation.
     */
    void reject(Command command);

    record Command(Long invitationId, Long requesterId) {
    }
}
