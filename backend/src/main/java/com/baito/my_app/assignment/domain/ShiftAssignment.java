package com.baito.my_app.assignment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A confirmed (or cancelled) assignment of a part-timer to a single 30-minute slot.
 */
@Getter
@Setter
@AllArgsConstructor
public class ShiftAssignment {

    private Long id;
    private Long groupId;
    private Long memberId;
    private LocalDate workDate;
    private LocalTime startTime;
    private Long assignedBy;
    private ShiftAssignmentStatus status;
    private LocalDateTime createdAt;

    public static ShiftAssignment confirm(Long groupId, Long memberId, LocalDate workDate,
                                          LocalTime startTime, Long assignedBy) {
        return new ShiftAssignment(null, groupId, memberId, workDate, startTime, assignedBy,
                ShiftAssignmentStatus.CONFIRMED, null);
    }
}
