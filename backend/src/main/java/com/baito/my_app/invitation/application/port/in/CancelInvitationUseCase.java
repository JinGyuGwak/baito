package com.baito.my_app.invitation.application.port.in;

public interface CancelInvitationUseCase {

    /**
     * Owner cancels a PENDING invitation they sent.
     */
    void cancel(Command command);

    record Command(Long invitationId, Long requesterId) {
    }
}
