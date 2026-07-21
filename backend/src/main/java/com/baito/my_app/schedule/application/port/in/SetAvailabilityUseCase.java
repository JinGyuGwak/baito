package com.baito.my_app.schedule.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Part-timer sets their available times for a group on a date. Replaces all existing availability
 * slots for that (group, member, date). Each interval is expanded into 30-minute slots.
 */
public interface SetAvailabilityUseCase {

    void setAvailability(Command command);

    @Getter
    @Setter
    @AllArgsConstructor
    class Command {
        private Long groupId;
        private Long memberId;
        private LocalDate workDate;
        private List<Interval> intervals;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    class Interval {
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
