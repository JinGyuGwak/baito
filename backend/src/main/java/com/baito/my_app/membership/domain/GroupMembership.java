package com.baito.my_app.membership.domain;

import java.time.LocalDateTime;

/**
 * Domain model for a confirmed membership of a part-timer in a group, created when an invitation
 * is accepted.
 */
public record GroupMembership(
        Long id,
        Long groupId,
        Long memberId,
        MembershipStatus status,
        LocalDateTime joinedAt
) {

    public static GroupMembership activate(Long groupId, Long memberId, LocalDateTime joinedAt) {
        return new GroupMembership(null, groupId, memberId, MembershipStatus.ACTIVE, joinedAt);
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }
}
