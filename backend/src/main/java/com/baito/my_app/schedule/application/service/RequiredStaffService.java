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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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
                throw new InvalidSlotTimeException("必要人数は0以上でなければなりません: " + interval.getRequiredCount());
            }
            for (LocalTime slotStart : SlotTimes.expand(interval.getStartTime(), interval.getEndTime())) {
                if (countByStart.putIfAbsent(slotStart, interval.getRequiredCount()) != null) {
                    throw new InvalidSlotTimeException("重複する時間帯があります: " + slotStart);
                }
            }
        }

        List<RequiredStaffSlot> slots = new ArrayList<>();
        countByStart.forEach((start, count) ->
                slots.add(RequiredStaffSlot.of(command.getGroupId(), command.getWorkDate(), start, count)));

        // Any staff assigned within a block whose shape changes would otherwise be left inconsistent, so we
        // clear the affected assignments together with the old required-staff rows before re-inserting.
        Set<LocalTime> newStarts = new HashSet<>(countByStart.keySet());
        List<LocalTime> existingSlots = requiredStaffSlotRepository
                .findByGroupIdAndWorkDate(command.getGroupId(), command.getWorkDate()).stream()
                .map(RequiredStaffSlot::getStartTime)
                .sorted()
                .toList();

        // Replace the whole day for this group.
        requiredStaffSlotRepository.deleteByGroupIdAndWorkDate(command.getGroupId(), command.getWorkDate());
        cancelAssignmentsInAffectedBlocks(command.getGroupId(), command.getWorkDate(), existingSlots, newStarts);
        requiredStaffSlotRepository.saveAll(slots);
    }

    /**
     * Cancels shift assignments in every required-staff block whose shape changed, treating a whole block as
     * one unit.
     *
     * <p>Required-staff slots are stored per 30-minute slot with no notion of an interval boundary, so
     * adjacent slots form a single continuous <em>block</em> (e.g. 09:00~12:00 and an adjacent 12:00~13:00
     * with the same headcount are indistinguishable from a single 09:00~13:00 block). A part-timer may be
     * assigned to only part of such a block.
     *
     * <p>We walk the union of the old and new required slots. A contiguous run whose old/new membership
     * disagrees on any slot is a block whose shape changed — because a slot was removed (the block shrinks or
     * splits) or a new adjacent slot merged onto it (the block grows). In either case <em>every</em>
     * assignment anywhere in that block is cancelled — not only the assignments on the changed slots. Runs
     * where old and new agree on every slot (e.g. only the headcount changed) keep their assignments.
     */
    private void cancelAssignmentsInAffectedBlocks(Long groupId, LocalDate workDate,
                                                   List<LocalTime> existingSlots, Set<LocalTime> newStarts) {
        Set<LocalTime> oldStarts = new HashSet<>(existingSlots);
        List<LocalTime> union = Stream.concat(existingSlots.stream(), newStarts.stream())
                .distinct()
                .sorted()
                .toList();

        List<LocalTime> slotsToCancel = new ArrayList<>();
        int blockStart = 0;
        while (blockStart < union.size()) {
            int blockEnd = blockStart;
            while (blockEnd + 1 < union.size()
                    && union.get(blockEnd + 1)
                            .equals(union.get(blockEnd).plusMinutes(SlotTimes.SLOT_MINUTES))) {
                blockEnd++;
            }
            List<LocalTime> block = union.subList(blockStart, blockEnd + 1);
            boolean shapeChanged = block.stream()
                    .anyMatch(slot -> oldStarts.contains(slot) != newStarts.contains(slot));
            if (shapeChanged) {
                // Only slots that previously had a required-staff row could carry assignments.
                block.stream().filter(oldStarts::contains).forEach(slotsToCancel::add);
            }
            blockStart = blockEnd + 1;
        }

        if (!slotsToCancel.isEmpty()) {
            shiftAssignmentRepository.deleteConfirmedInSlotsForAllMembers(groupId, workDate, slotsToCancel);
        }
    }
}
