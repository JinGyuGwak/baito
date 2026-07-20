package com.baito.my_app.schedule.domain;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A 30-minute slot in which a part-timer is available to work for a group.
 * Row present = available; absent = unavailable.
 */
public record AvailabilitySlot(
        Long id,
        Long groupId,
        Long memberId,
        LocalDate workDate,
        LocalTime startTime
) {

    public static AvailabilitySlot of(Long groupId, Long memberId, LocalDate workDate, LocalTime startTime) {
        return new AvailabilitySlot(null, groupId, memberId, workDate, startTime);
    }
}
