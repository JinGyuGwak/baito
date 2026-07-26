package com.baito.my_app.schedule.application.service;

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

@Service
@Transactional
public class RequiredStaffService implements SetRequiredStaffUseCase {

    private final WorkGroupRepository workGroupRepository;
    private final RequiredStaffSlotRepository requiredStaffSlotRepository;

    public RequiredStaffService(WorkGroupRepository workGroupRepository,
                                RequiredStaffSlotRepository requiredStaffSlotRepository) {
        this.workGroupRepository = workGroupRepository;
        this.requiredStaffSlotRepository = requiredStaffSlotRepository;
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

        // Replace the whole day for this group.
        requiredStaffSlotRepository.deleteByGroupIdAndWorkDate(command.getGroupId(), command.getWorkDate());
        requiredStaffSlotRepository.saveAll(slots);
    }
}
