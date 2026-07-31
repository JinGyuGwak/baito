package com.baito.my_app.membership.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model for a confirmed membership of a part-timer in a group, created when an invitation
 * is accepted.
 */
@Getter
@Setter
@AllArgsConstructor
public class GroupMembership {

    private Long id;
    private Long groupId;
    private Long memberId;
    private MembershipStatus status;
    private LocalDateTime joinedAt;

    public static GroupMembership activate(Long groupId, Long memberId, LocalDateTime joinedAt) {
        return new GroupMembership(null, groupId, memberId, MembershipStatus.ACTIVE, joinedAt);
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }

    /** Kicked from the group (or left) — keeps the row for history, blocks further scheduling. */
    public void deactivate() {
        this.status = MembershipStatus.INACTIVE;
    }
}
