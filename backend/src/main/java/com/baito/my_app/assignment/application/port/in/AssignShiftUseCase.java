package com.baito.my_app.assignment.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Owner assigns a part-timer to a time range. The range is decomposed into 30-minute slots and
 * every slot must pass availability and quota checks before any assignment is persisted (all-or-nothing).
 */
public interface AssignShiftUseCase {

    /**
     * @return the number of newly created slot assignments
     */
    int assign(Command command);

    @Getter
    @Setter
    @AllArgsConstructor
    class Command {
        private Long groupId;
        private Long assignerId;
        private Long memberId;
        private LocalDate workDate;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
