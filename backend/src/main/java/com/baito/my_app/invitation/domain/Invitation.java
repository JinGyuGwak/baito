package com.baito.my_app.invitation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model for an invitation. State transitions validate the current state and mutate this
 * instance in place (history rows are never deleted, only their status changes).
 */
@Getter
@Setter
@AllArgsConstructor
public class Invitation {

    private Long id;
    private Long groupId;
    private Long inviterId;
    private Long inviteeId;
    private InvitationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    public static Invitation create(Long groupId, Long inviterId, Long inviteeId) {
        return new Invitation(null, groupId, inviterId, inviteeId, InvitationStatus.PENDING, null, null);
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    public void accept(LocalDateTime respondedAt) {
        transitionTo(InvitationStatus.ACCEPTED, respondedAt);
    }

    public void reject(LocalDateTime respondedAt) {
        transitionTo(InvitationStatus.REJECTED, respondedAt);
    }

    public void cancel(LocalDateTime respondedAt) {
        transitionTo(InvitationStatus.CANCELLED, respondedAt);
    }

    private void transitionTo(InvitationStatus next, LocalDateTime respondedAt) {
        if (!isPending()) {
            throw new InvalidInvitationStateException(status);
        }
        this.status = next;
        this.respondedAt = respondedAt;
    }
}
