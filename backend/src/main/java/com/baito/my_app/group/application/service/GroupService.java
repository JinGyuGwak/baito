package com.baito.my_app.group.application.service;

import com.baito.my_app.group.application.port.in.CreateGroupUseCase;
import com.baito.my_app.group.application.port.in.GetOwnedGroupsQuery;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.WorkGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GroupService implements CreateGroupUseCase, GetOwnedGroupsQuery {

    private final WorkGroupRepository workGroupRepository;

    public GroupService(WorkGroupRepository workGroupRepository) {
        this.workGroupRepository = workGroupRepository;
    }

    @Override
    public Long createGroup(Command command) {
        WorkGroup group = WorkGroup.create(command.ownerId(), command.name(), command.description());
        return workGroupRepository.save(group).id();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkGroup> getOwnedGroups(Long ownerId) {
        return workGroupRepository.findByOwnerId(ownerId);
    }
}
