package com.baito.my_app.schedule.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Required headcount for one 30-minute slot of a group on a date.
 */
@Getter
@Setter
@AllArgsConstructor
public class RequiredStaffSlot {

    private Long id;
    private Long groupId;
    private LocalDate workDate;
    private LocalTime startTime;
    private int requiredCount;

    public static RequiredStaffSlot of(Long groupId, LocalDate workDate, LocalTime startTime, int requiredCount) {
        return new RequiredStaffSlot(null, groupId, workDate, startTime, requiredCount);
    }
}
