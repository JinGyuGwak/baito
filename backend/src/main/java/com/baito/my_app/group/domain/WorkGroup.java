package com.baito.my_app.group.domain;

import java.time.LocalDateTime;

/**
 * Domain model for a group / store owned by an OWNER member.
 */
public record WorkGroup(
        Long id,
        Long ownerId,
        String name,
        String description,
        LocalDateTime createdAt
) {

    public static WorkGroup create(Long ownerId, String name, String description) {
        return new WorkGroup(null, ownerId, name, description, null);
    }

    public boolean isOwnedBy(Long memberId) {
        return ownerId.equals(memberId);
    }
}
