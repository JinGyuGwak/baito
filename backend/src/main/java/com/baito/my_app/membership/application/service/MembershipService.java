package com.baito.my_app.membership.application.service;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.membership.application.port.in.RemoveMemberUseCase;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import com.baito.my_app.membership.domain.NotGroupMemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MembershipService implements RemoveMemberUseCase {

    private final WorkGroupRepository workGroupRepository;
    private final GroupMembershipRepository membershipRepository;

    public MembershipService(WorkGroupRepository workGroupRepository,
                             GroupMembershipRepository membershipRepository) {
        this.workGroupRepository = workGroupRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    public void remove(Command command) {
        WorkGroup group = workGroupRepository.findById(command.getGroupId())
                .orElseThrow(() -> new NotGroupOwnerException(command.getGroupId()));
        if (!group.isOwnedBy(command.getOwnerId())) {
            throw new NotGroupOwnerException(command.getGroupId());
        }

        GroupMembership membership = membershipRepository
                .findByGroupIdAndMemberId(command.getGroupId(), command.getMemberId())
                .filter(GroupMembership::isActive)
                .orElseThrow(() -> new NotGroupMemberException(command.getGroupId()));

        membership.deactivate();
        membershipRepository.save(membership);
    }
}
