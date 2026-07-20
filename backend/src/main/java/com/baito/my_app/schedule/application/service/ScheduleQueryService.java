package com.baito.my_app.schedule.application.service;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.NotGroupMemberException;
import com.baito.my_app.schedule.application.port.in.GetScheduleQuery;
import com.baito.my_app.schedule.application.port.out.AvailabilitySlotRepository;
import com.baito.my_app.schedule.application.port.out.RequiredStaffSlotRepository;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ScheduleQueryService implements GetScheduleQuery {

    private final WorkGroupRepository workGroupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final RequiredStaffSlotRepository requiredStaffSlotRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    public ScheduleQueryService(WorkGroupRepository workGroupRepository,
                                GroupMembershipRepository membershipRepository,
                                RequiredStaffSlotRepository requiredStaffSlotRepository,
                                AvailabilitySlotRepository availabilitySlotRepository) {
        this.workGroupRepository = workGroupRepository;
        this.membershipRepository = membershipRepository;
        this.requiredStaffSlotRepository = requiredStaffSlotRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    @Override
    public List<RequiredStaffSlot> getRequiredStaff(Long groupId, Long ownerId, LocalDate workDate) {
        WorkGroup group = workGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotGroupOwnerException(groupId));
        if (!group.isOwnedBy(ownerId)) {
            throw new NotGroupOwnerException(groupId);
        }
        return requiredStaffSlotRepository.findByGroupIdAndWorkDate(groupId, workDate);
    }

    @Override
    public List<AvailabilitySlot> getMyAvailability(Long groupId, Long memberId, LocalDate workDate) {
        if (!membershipRepository.existsActiveMembership(groupId, memberId)) {
            throw new NotGroupMemberException(groupId);
        }
        return availabilitySlotRepository.findByGroupIdAndMemberIdAndWorkDate(groupId, memberId, workDate);
    }
}
