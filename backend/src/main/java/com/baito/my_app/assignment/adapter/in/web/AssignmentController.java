package com.baito.my_app.assignment.adapter.in.web;

import com.baito.my_app.assignment.application.port.in.AssignShiftUseCase;
import com.baito.my_app.assignment.application.port.in.CancelShiftUseCase;
import com.baito.my_app.assignment.application.port.in.GetAssignmentCandidatesQuery;
import com.baito.my_app.assignment.application.port.in.GetAssignmentsQuery;
import com.baito.my_app.common.security.LoginMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
    private final CancelShiftUseCase cancelShiftUseCase;
    private final GetAssignmentsQuery getAssignmentsQuery;
    private final GetAssignmentCandidatesQuery getAssignmentCandidatesQuery;

    public AssignmentController(AssignShiftUseCase assignShiftUseCase,
                                CancelShiftUseCase cancelShiftUseCase,
                                GetAssignmentsQuery getAssignmentsQuery,
                                GetAssignmentCandidatesQuery getAssignmentCandidatesQuery) {
        this.assignShiftUseCase = assignShiftUseCase;
        this.cancelShiftUseCase = cancelShiftUseCase;
        this.getAssignmentsQuery = getAssignmentsQuery;
        this.getAssignmentCandidatesQuery = getAssignmentCandidatesQuery;
    }

    @PostMapping
    public AssignResponse assign(@PathVariable Long groupId,
                                 @Valid @RequestBody AssignRequest request,
                                 @AuthenticationPrincipal LoginMember loginMember) {
        int assigned = assignShiftUseCase.assign(new AssignShiftUseCase.Command(
                groupId, loginMember.getMemberId(), request.getMemberId(),
                request.getWorkDate(), request.getStartTime(), request.getEndTime()));
        return new AssignResponse(assigned);
    }

    @PostMapping("/cancel")
    public CancelResponse cancel(@PathVariable Long groupId,
                                 @Valid @RequestBody CancelRequest request,
                                 @AuthenticationPrincipal LoginMember loginMember) {
        int cancelled = cancelShiftUseCase.cancel(new CancelShiftUseCase.Command(
                groupId, loginMember.getMemberId(), request.getMemberId(),
                request.getWorkDate(), request.getStartTime(), request.getEndTime()));
        return new CancelResponse(cancelled);
    }

    @GetMapping
    public List<AssignmentResponse> get(@PathVariable Long groupId,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                        @AuthenticationPrincipal LoginMember loginMember) {
        return getAssignmentsQuery.getAssignments(groupId, loginMember.getMemberId(), date).stream()
                .map(AssignmentResponse::from)
                .toList();
    }

    /** Part-timers able to work the given required-staff block (availability covers every slot). */
    @GetMapping("/candidates")
    public List<CandidateResponse> candidates(@PathVariable Long groupId,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                                              @AuthenticationPrincipal LoginMember loginMember) {
        return getAssignmentCandidatesQuery
                .getCandidates(groupId, loginMember.getMemberId(), date, startTime, endTime).stream()
                .map(CandidateResponse::from)
                .toList();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AssignRequest {
        @NotNull
        private Long memberId;
        @NotNull
        private LocalDate workDate;
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AssignResponse {
        private int assignedSlotCount;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CancelRequest {
        @NotNull
        private Long memberId;
        @NotNull
        private LocalDate workDate;
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CancelResponse {
        private int cancelledSlotCount;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AssignmentResponse {
        private Long memberId;
        private String memberName;
        private String memberLoginId;
        private LocalTime startTime;

        static AssignmentResponse from(GetAssignmentsQuery.AssignmentDetail d) {
            return new AssignmentResponse(d.memberId(), d.memberName(), d.memberLoginId(), d.startTime());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CandidateResponse {
        private Long memberId;
        private String name;
        private String loginId;
        private List<IntervalResponse> availableIntervals;
        private boolean alreadyAssigned;

        static CandidateResponse from(GetAssignmentCandidatesQuery.Candidate c) {
            return new CandidateResponse(c.memberId(), c.name(), c.loginId(),
                    c.availableIntervals().stream()
                            .map(iv -> new IntervalResponse(iv.startTime(), iv.endTime()))
                            .toList(),
                    c.alreadyAssigned());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class IntervalResponse {
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
