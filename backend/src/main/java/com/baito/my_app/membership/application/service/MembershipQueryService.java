package com.baito.my_app.membership.application.service;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.membership.application.port.in.GetJoinedGroupsQuery;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MembershipQueryService implements GetJoinedGroupsQuery {

    private final GroupMembershipRepository membershipRepository;
    private final WorkGroupRepository workGroupRepository;

    public MembershipQueryService(GroupMembershipRepository membershipRepository,
                                  WorkGroupRepository workGroupRepository) {
        this.membershipRepository = membershipRepository;
        this.workGroupRepository = workGroupRepository;
    }

    @Override
    public List<WorkGroup> getJoinedGroups(Long memberId) {
        return membershipRepository.findActiveByMemberId(memberId).stream()
                .map(GroupMembership::getGroupId)
                .map(workGroupRepository::findById)
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}
