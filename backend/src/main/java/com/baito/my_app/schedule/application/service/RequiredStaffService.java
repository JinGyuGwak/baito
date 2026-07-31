package com.baito.my_app.schedule.application.service;

import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.common.domain.SlotTimes;
import com.baito.my_app.common.exception.InvalidSlotTimeException;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.schedule.application.port.in.SetRequiredStaffUseCase;
import com.baito.my_app.schedule.application.port.out.RequiredStaffSlotRepository;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class RequiredStaffService implements SetRequiredStaffUseCase {

    private final WorkGroupRepository workGroupRepository;
    private final RequiredStaffSlotRepository requiredStaffSlotRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    public RequiredStaffService(WorkGroupRepository workGroupRepository,
                                RequiredStaffSlotRepository requiredStaffSlotRepository,
                                ShiftAssignmentRepository shiftAssignmentRepository) {
        this.workGroupRepository = workGroupRepository;
        this.requiredStaffSlotRepository = requiredStaffSlotRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

    @Override
    public void setRequiredStaff(Command command) {
        WorkGroup group = workGroupRepository.findById(command.getGroupId())
                .orElseThrow(() -> new NotGroupOwnerException(command.getGroupId()));
        if (!group.isOwnedBy(command.getOwnerId())) {
            throw new NotGroupOwnerException(command.getGroupId());
        }

        // Expand every interval to 30-minute slots; reject overlaps (a slot appearing in two intervals).
        Map<LocalTime, Integer> countByStart = new LinkedHashMap<>();
        for (Interval interval : command.getIntervals()) {
            if (interval.getRequiredCount() < 0) {
                throw new InvalidSlotTimeException("필요 인원은 0 이상이어야 합니다: " + interval.getRequiredCount());
            }
            for (LocalTime slotStart : SlotTimes.expand(interval.getStartTime(), interval.getEndTime())) {
                if (countByStart.putIfAbsent(slotStart, interval.getRequiredCount()) != null) {
                    throw new InvalidSlotTimeException("겹치는 시간대가 있습니다: " + slotStart);
                }
            }
        }

        List<RequiredStaffSlot> slots = new ArrayList<>();
        countByStart.forEach((start, count) ->
                slots.add(RequiredStaffSlot.of(command.getGroupId(), command.getWorkDate(), start, count)));

        // A slot that existed before but is absent from the new configuration is "removed" (its time was
        // changed or the slot was deleted). Any staff already assigned to such a slot would otherwise be
        // orphaned, so we clear those assignments together with the old required-staff rows before re-inserting.
        Set<LocalTime> newStarts = countByStart.keySet();
        List<LocalTime> removedSlots = requiredStaffSlotRepository
                .findByGroupIdAndWorkDate(command.getGroupId(), command.getWorkDate()).stream()
                .map(RequiredStaffSlot::getStartTime)
                .filter(start -> !newStarts.contains(start))
                .toList();

        // Replace the whole day for this group.
        requiredStaffSlotRepository.deleteByGroupIdAndWorkDate(command.getGroupId(), command.getWorkDate());
        shiftAssignmentRepository.deleteConfirmedInSlotsForAllMembers(
                command.getGroupId(), command.getWorkDate(), removedSlots);
        requiredStaffSlotRepository.saveAll(slots);
    }
}
