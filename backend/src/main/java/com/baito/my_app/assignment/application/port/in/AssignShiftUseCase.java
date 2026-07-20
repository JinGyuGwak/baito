package com.baito.my_app.assignment.application.port.in;

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

    record Command(
            Long groupId,
            Long assignerId,
            Long memberId,
            LocalDate workDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
    }
}
