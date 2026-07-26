package com.baito.my_app.assignment.application.port.in;

import com.baito.my_app.assignment.domain.ShiftAssignment;

import java.time.LocalDate;
import java.util.List;

public interface GetAssignmentsQuery {

    /** Owner view: confirmed assignments for a group on a date. */
    List<ShiftAssignment> getAssignments(Long groupId, Long ownerId, LocalDate workDate);

    /** Part-timer view: the member's own confirmed assignments on a date, across all their groups. */
    List<ShiftAssignment> getMyAssignments(Long memberId, LocalDate workDate);
}
