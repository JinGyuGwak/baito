package com.baito.my_app.assignment.adapter.in.web;

import com.baito.my_app.assignment.application.port.in.GetAssignmentsQuery;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.common.security.LoginMember;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Part-timer's own confirmed schedule. Unlike {@link AssignmentController} (owner-only),
 * this returns the logged-in member's CONFIRMED assignments across all their groups.
 */
@RestController
@RequestMapping("/api/me/assignments")
@PreAuthorize("hasRole('PART_TIMER')")
public class MyAssignmentController {

    private final GetAssignmentsQuery getAssignmentsQuery;

    public MyAssignmentController(GetAssignmentsQuery getAssignmentsQuery) {
        this.getAssignmentsQuery = getAssignmentsQuery;
    }

    @GetMapping
    public List<MyAssignmentResponse> getMySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal LoginMember loginMember) {
        return getAssignmentsQuery.getMyAssignments(loginMember.getMemberId(), date).stream()
                .map(MyAssignmentResponse::from)
                .toList();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class MyAssignmentResponse {
        private Long groupId;
        private LocalTime startTime;

        static MyAssignmentResponse from(ShiftAssignment a) {
            return new MyAssignmentResponse(a.getGroupId(), a.getStartTime());
        }
    }
}
