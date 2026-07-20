package com.baito.my_app.schedule.domain;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Required headcount for one 30-minute slot of a group on a date.
 */
public record RequiredStaffSlot(
        Long id,
        Long groupId,
        LocalDate workDate,
        LocalTime startTime,
        int requiredCount
) {

    public static RequiredStaffSlot of(Long groupId, LocalDate workDate, LocalTime startTime, int requiredCount) {
        return new RequiredStaffSlot(null, groupId, workDate, startTime, requiredCount);
    }
}
