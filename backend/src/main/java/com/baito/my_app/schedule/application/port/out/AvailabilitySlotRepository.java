package com.baito.my_app.schedule.application.port.out;

import com.baito.my_app.schedule.domain.AvailabilitySlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Outbound port for availability slots.
 */
public interface AvailabilitySlotRepository {

    void saveAll(List<AvailabilitySlot> slots);

    void deleteByGroupIdAndMemberIdAndWorkDate(Long groupId, Long memberId, LocalDate workDate);

    List<AvailabilitySlot> findByGroupIdAndMemberIdAndWorkDate(Long groupId, Long memberId, LocalDate workDate);

    /** All members' availability for a group on a date — used to build assignment candidate lists. */
    List<AvailabilitySlot> findByGroupIdAndWorkDate(Long groupId, LocalDate workDate);

    boolean existsByGroupIdAndMemberIdAndWorkDateAndStartTime(Long groupId, Long memberId, LocalDate workDate, LocalTime startTime);
}
