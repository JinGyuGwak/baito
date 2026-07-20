package com.baito.my_app.schedule.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Part-timer sets their available times for a group on a date. Replaces all existing availability
 * slots for that (group, member, date). Each interval is expanded into 30-minute slots.
 */
public interface SetAvailabilityUseCase {

    void setAvailability(Command command);

    record Command(Long groupId, Long memberId, LocalDate workDate, List<Interval> intervals) {
    }

    record Interval(LocalTime startTime, LocalTime endTime) {
    }
}
