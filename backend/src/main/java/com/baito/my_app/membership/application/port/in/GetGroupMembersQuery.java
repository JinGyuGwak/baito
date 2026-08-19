package com.baito.my_app.membership.application.port.in;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Owner view of a group's roster: active part-timer memberships joined with each member's identity.
 */
public interface GetGroupMembersQuery {

    List<GroupMember> getGroupMembers(Long groupId, Long ownerId);

    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode
    class GroupMember {
        private Long memberId;
        private String name;
        private String loginId;
        private LocalDateTime joinedAt;
    }
}
