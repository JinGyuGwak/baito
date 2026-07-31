package com.baito.my_app.membership.application.port.in;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Owner view of a group's roster: active part-timer memberships joined with each member's identity.
 */
public interface GetGroupMembersQuery {

    List<GroupMember> getGroupMembers(Long groupId, Long ownerId);

    record GroupMember(Long memberId, String name, String loginId, LocalDateTime joinedAt) {
    }
}
