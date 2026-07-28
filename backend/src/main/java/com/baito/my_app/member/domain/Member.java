package com.baito.my_app.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model for a member. {@code password} holds the already-encoded hash.
 * {@code id} is null until persisted.
 */
@Getter
@Setter
@AllArgsConstructor
public class Member {

    private Long id;
    private String loginId;
    private String password;
    private String name;
    private Role role;
    private LocalDateTime createdAt;

    /**
     * Factory for a not-yet-persisted member. {@code encodedPassword} must already be hashed.
     */
    public static Member register(String loginId, String encodedPassword, String name, Role role) {
        return new Member(null, loginId, encodedPassword, name, role, null);
    }

    public void changeName(String name) {
        this.name = name;
    }

    public boolean isOwner() {
        return role == Role.OWNER;
    }

    public boolean isPartTimer() {
        return role == Role.PART_TIMER;
    }
}
