package com.baito.my_app.invitation.domain;

import java.time.LocalDateTime;

/**
 * Domain model for an invitation. State transitions are pure: they validate the current state and
 * return a new instance (history rows are never deleted, only their status changes).
 */
public record Invitation(
        Long id,
        Long groupId,
        Long inviterId,
        Long inviteeId,
        InvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime respondedAt
) {

    public static Invitation create(Long groupId, Long inviterId, Long inviteeId) {
        return new Invitation(null, groupId, inviterId, inviteeId, InvitationStatus.PENDING, null, null);
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    public Invitation accept(LocalDateTime respondedAt) {
        return transitionTo(InvitationStatus.ACCEPTED, respondedAt);
    }

    public Invitation reject(LocalDateTime respondedAt) {
        return transitionTo(InvitationStatus.REJECTED, respondedAt);
    }

    public Invitation cancel(LocalDateTime respondedAt) {
        return transitionTo(InvitationStatus.CANCELLED, respondedAt);
    }

    private Invitation transitionTo(InvitationStatus next, LocalDateTime respondedAt) {
        if (!isPending()) {
            throw new InvalidInvitationStateException(status);
        }
        return new Invitation(id, groupId, inviterId, inviteeId, next, createdAt, respondedAt);
    }
}
