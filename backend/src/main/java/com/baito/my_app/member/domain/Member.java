package com.baito.my_app.member.domain;

import java.time.LocalDateTime;

/**
 * Domain model for a member. {@code password} holds the already-encoded hash.
 * {@code id} is null until persisted.
 */
public record Member(
        Long id,
        String loginId,
        String password,
        String name,
        Role role,
        LocalDateTime createdAt
) {

    /**
     * Factory for a not-yet-persisted member. {@code encodedPassword} must already be hashed.
     */
    public static Member register(String loginId, String encodedPassword, String name, Role role) {
        return new Member(null, loginId, encodedPassword, name, role, null);
    }

    public boolean isOwner() {
        return role == Role.OWNER;
    }

    public boolean isPartTimer() {
        return role == Role.PART_TIMER;
    }
}
