package com.baito.my_app.assignment.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A confirmed (or cancelled) assignment of a part-timer to a single 30-minute slot.
 */
public record ShiftAssignment(
        Long id,
        Long groupId,
        Long memberId,
        LocalDate workDate,
        LocalTime startTime,
        Long assignedBy,
        ShiftAssignmentStatus status,
        LocalDateTime createdAt
) {

    public static ShiftAssignment confirm(Long groupId, Long memberId, LocalDate workDate,
                                          LocalTime startTime, Long assignedBy) {
        return new ShiftAssignment(null, groupId, memberId, workDate, startTime, assignedBy,
                ShiftAssignmentStatus.CONFIRMED, null);
    }
}
