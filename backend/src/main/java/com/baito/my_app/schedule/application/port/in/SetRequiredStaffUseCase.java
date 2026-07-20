package com.baito.my_app.schedule.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Owner sets the required headcount for a group on a date. Replaces all existing required-staff
 * slots for that (group, date). Each interval is expanded into 30-minute slots.
 */
public interface SetRequiredStaffUseCase {

    void setRequiredStaff(Command command);

    record Command(Long groupId, Long ownerId, LocalDate workDate, List<Interval> intervals) {
    }

    record Interval(LocalTime startTime, LocalTime endTime, int requiredCount) {
    }
}
