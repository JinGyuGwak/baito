package com.baito.my_app.group.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model for a group / store owned by an OWNER member.
 */
@Getter
@Setter
@AllArgsConstructor
public class WorkGroup {

    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    public static WorkGroup create(Long ownerId, String name, String description) {
        return new WorkGroup(null, ownerId, name, description, null);
    }

    public boolean isOwnedBy(Long memberId) {
        return ownerId.equals(memberId);
    }
}
