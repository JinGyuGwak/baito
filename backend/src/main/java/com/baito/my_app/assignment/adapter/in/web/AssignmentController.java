package com.baito.my_app.assignment.adapter.in.web;

import com.baito.my_app.assignment.application.port.in.AssignShiftUseCase;
import com.baito.my_app.assignment.application.port.in.GetAssignmentsQuery;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.common.security.LoginMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/assignments")
@PreAuthorize("hasRole('OWNER')")
public class AssignmentController {

    private final AssignShiftUseCase assignShiftUseCase;
    private final GetAssignmentsQuery getAssignmentsQuery;

    public AssignmentController(AssignShiftUseCase assignShiftUseCase, GetAssignmentsQuery getAssignmentsQuery) {
        this.assignShiftUseCase = assignShiftUseCase;
        this.getAssignmentsQuery = getAssignmentsQuery;
    }

    @PostMapping
    public AssignResponse assign(@PathVariable Long groupId,
                                 @Valid @RequestBody AssignRequest request,
                                 @AuthenticationPrincipal LoginMember loginMember) {
        int assigned = assignShiftUseCase.assign(new AssignShiftUseCase.Command(
                groupId, loginMember.getMemberId(), request.memberId(),
                request.workDate(), request.startTime(), request.endTime()));
        return new AssignResponse(assigned);
    }

    @GetMapping
    public List<AssignmentResponse> get(@PathVariable Long groupId,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                        @AuthenticationPrincipal LoginMember loginMember) {
        return getAssignmentsQuery.getAssignments(groupId, loginMember.getMemberId(), date).stream()
                .map(AssignmentResponse::from)
                .toList();
    }

    public record AssignRequest(
            @NotNull Long memberId,
            @NotNull LocalDate workDate,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime
    ) {
    }

    public record AssignResponse(int assignedSlotCount) {
    }

    public record AssignmentResponse(Long memberId, LocalTime startTime) {
        static AssignmentResponse from(ShiftAssignment a) {
            return new AssignmentResponse(a.memberId(), a.startTime());
        }
    }
}
