package com.baito.my_app.assignment.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Owner cancels a part-timer's confirmed assignment over a time range. Every CONFIRMED slot in
 * the range is deleted; slots that were never assigned are silently skipped.
 */
public interface CancelShiftUseCase {

    /**
     * @return the number of slot assignments that were cancelled
     */
    int cancel(Command command);

    @Getter
    @Setter
    @AllArgsConstructor
    class Command {
        private Long groupId;
        private Long ownerId;
        private Long memberId;
        private LocalDate workDate;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
