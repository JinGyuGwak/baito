package com.baito.my_app.group.application.port.in;

import com.baito.my_app.group.domain.WorkGroup;

import java.util.List;

public interface GetOwnedGroupsQuery {

    List<WorkGroup> getOwnedGroups(Long ownerId);
}
