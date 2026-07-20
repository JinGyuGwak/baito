package com.baito.my_app.membership.application.port.in;

import com.baito.my_app.group.domain.WorkGroup;

import java.util.List;

/**
 * Inbound port: list the groups a part-timer actively belongs to (so they can pick one for availability).
 */
public interface GetJoinedGroupsQuery {

    List<WorkGroup> getJoinedGroups(Long memberId);
}
