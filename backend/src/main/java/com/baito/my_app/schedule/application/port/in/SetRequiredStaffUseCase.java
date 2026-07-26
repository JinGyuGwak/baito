package com.baito.my_app.schedule.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Owner sets the required headcount for a group on a date. Replaces all existing required-staff
 * slots for that (group, date). Each interval is expanded into 30-minute slots.
 */
public interface SetRequiredStaffUseCase {

    void setRequiredStaff(Command command);

    @Getter
    @Setter
    @AllArgsConstructor
    class Command {
        private Long groupId;
        private Long ownerId;
        private LocalDate workDate;
        private List<Interval> intervals;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    class Interval {
        private LocalTime startTime;
        private LocalTime endTime;
        private int requiredCount;
    }
}
