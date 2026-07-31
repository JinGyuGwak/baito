package com.baito.my_app.membership.application.port.out;

import com.baito.my_app.membership.domain.GroupMembership;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for confirmed group memberships.
 */
public interface GroupMembershipRepository {

    GroupMembership save(GroupMembership membership);

    Optional<GroupMembership> findByGroupIdAndMemberId(Long groupId, Long memberId);

    boolean existsActiveMembership(Long groupId, Long memberId);

    List<GroupMembership> findActiveByMemberId(Long memberId);

    /** Active part-timer memberships of a group — the group's roster. */
    List<GroupMembership> findActiveByGroupId(Long groupId);
}
