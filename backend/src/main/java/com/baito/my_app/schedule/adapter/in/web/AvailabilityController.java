package com.baito.my_app.schedule.adapter.in.web;

import com.baito.my_app.common.security.LoginMember;
import com.baito.my_app.schedule.application.port.in.GetScheduleQuery;
import com.baito.my_app.schedule.application.port.in.SetAvailabilityUseCase;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/groups/{groupId}/availability")
@PreAuthorize("hasRole('PART_TIMER')")
public class AvailabilityController {

    private final SetAvailabilityUseCase setAvailabilityUseCase;
    private final GetScheduleQuery getScheduleQuery;

    public AvailabilityController(SetAvailabilityUseCase setAvailabilityUseCase, GetScheduleQuery getScheduleQuery) {
        this.setAvailabilityUseCase = setAvailabilityUseCase;
        this.getScheduleQuery = getScheduleQuery;
    }

    /** Replaces the part-timer's availability for the given day. */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void set(@PathVariable Long groupId,
                    @Valid @RequestBody SetAvailabilityRequest request,
                    @AuthenticationPrincipal LoginMember loginMember) {
        List<SetAvailabilityUseCase.Interval> intervals = request.intervals().stream()
                .map(i -> new SetAvailabilityUseCase.Interval(i.startTime(), i.endTime()))
                .toList();
        setAvailabilityUseCase.setAvailability(new SetAvailabilityUseCase.Command(
                groupId, loginMember.getMemberId(), request.workDate(), intervals));
    }

    @GetMapping
    public List<AvailabilityResponse> get(@PathVariable Long groupId,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                          @AuthenticationPrincipal LoginMember loginMember) {
        return getScheduleQuery.getMyAvailability(groupId, loginMember.getMemberId(), date).stream()
                .map(AvailabilityResponse::from)
                .toList();
    }

    public record SetAvailabilityRequest(
            @NotNull LocalDate workDate,
            @NotEmpty @Valid List<IntervalRequest> intervals
    ) {
    }

    public record IntervalRequest(
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime
    ) {
    }

    public record AvailabilityResponse(LocalTime startTime) {
        static AvailabilityResponse from(AvailabilitySlot slot) {
            return new AvailabilityResponse(slot.startTime());
        }
    }
}
