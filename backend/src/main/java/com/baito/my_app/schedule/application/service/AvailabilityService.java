package com.baito.my_app.schedule.application.service;

import com.baito.my_app.common.domain.SlotTimes;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.NotGroupMemberException;
import com.baito.my_app.schedule.application.port.in.SetAvailabilityUseCase;
import com.baito.my_app.schedule.application.port.out.AvailabilitySlotRepository;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AvailabilityService implements SetAvailabilityUseCase {

    private final GroupMembershipRepository membershipRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    public AvailabilityService(GroupMembershipRepository membershipRepository,
                               AvailabilitySlotRepository availabilitySlotRepository) {
        this.membershipRepository = membershipRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    @Override
    public void setAvailability(Command command) {
        if (!membershipRepository.existsActiveMembership(command.groupId(), command.memberId())) {
            throw new NotGroupMemberException(command.groupId());
        }

        // Expand every interval to 30-minute slots; overlapping intervals simply union.
        Set<LocalTime> starts = new LinkedHashSet<>();
        for (Interval interval : command.intervals()) {
            starts.addAll(SlotTimes.expand(interval.startTime(), interval.endTime()));
        }

        List<AvailabilitySlot> slots = starts.stream()
                .map(start -> AvailabilitySlot.of(command.groupId(), command.memberId(), command.workDate(), start))
                .toList();

        // Replace the whole day for this (group, member).
        availabilitySlotRepository.deleteByGroupIdAndMemberIdAndWorkDate(
                command.groupId(), command.memberId(), command.workDate());
        availabilitySlotRepository.saveAll(slots);
    }
}
