package com.baito.my_app.schedule.adapter.in.web;

import com.baito.my_app.common.security.LoginMember;
import com.baito.my_app.schedule.application.port.in.GetScheduleQuery;
import com.baito.my_app.schedule.application.port.in.SetRequiredStaffUseCase;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/required-staff")
@PreAuthorize("hasRole('OWNER')")
public class RequiredStaffController {

    private final SetRequiredStaffUseCase setRequiredStaffUseCase;
    private final GetScheduleQuery getScheduleQuery;

    public RequiredStaffController(SetRequiredStaffUseCase setRequiredStaffUseCase, GetScheduleQuery getScheduleQuery) {
        this.setRequiredStaffUseCase = setRequiredStaffUseCase;
        this.getScheduleQuery = getScheduleQuery;
    }

    /** Replaces the required-staff configuration for the given day. */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void set(@PathVariable Long groupId,
                    @Valid @RequestBody SetRequiredStaffRequest request,
                    @AuthenticationPrincipal LoginMember loginMember) {
        List<SetRequiredStaffUseCase.Interval> intervals = request.intervals().stream()
                .map(i -> new SetRequiredStaffUseCase.Interval(i.startTime(), i.endTime(), i.requiredCount()))
                .toList();
        setRequiredStaffUseCase.setRequiredStaff(new SetRequiredStaffUseCase.Command(
                groupId, loginMember.getMemberId(), request.workDate(), intervals));
    }

    @GetMapping
    public List<RequiredStaffResponse> get(@PathVariable Long groupId,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                           @AuthenticationPrincipal LoginMember loginMember) {
        return getScheduleQuery.getRequiredStaff(groupId, loginMember.getMemberId(), date).stream()
                .map(RequiredStaffResponse::from)
                .toList();
    }

    public record SetRequiredStaffRequest(
            @NotNull LocalDate workDate,
            @NotEmpty @Valid List<IntervalRequest> intervals
    ) {
    }

    public record IntervalRequest(
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            @PositiveOrZero int requiredCount
    ) {
    }

    public record RequiredStaffResponse(LocalTime startTime, int requiredCount) {
        static RequiredStaffResponse from(RequiredStaffSlot slot) {
            return new RequiredStaffResponse(slot.startTime(), slot.requiredCount());
        }
    }
}
