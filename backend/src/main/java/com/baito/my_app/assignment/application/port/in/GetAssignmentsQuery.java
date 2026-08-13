package com.baito.my_app.assignment.application.port.in;

import com.baito.my_app.assignment.domain.ShiftAssignment;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface GetAssignmentsQuery {

    /** Owner view: confirmed assignments for a group on a date, enriched with member identity. */
    List<AssignmentDetail> getAssignments(Long groupId, Long ownerId, LocalDate workDate);

    /** Part-timer view: the member's own confirmed assignments on a date, across all their groups. */
    List<ShiftAssignment> getMyAssignments(Long memberId, LocalDate workDate);

    /** One confirmed 30-minute slot with the assigned member's name and loginId. */
    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode
    class AssignmentDetail {
        private Long memberId;
        private String memberName;
        private String memberLoginId;
        private LocalTime startTime;
    }
}
