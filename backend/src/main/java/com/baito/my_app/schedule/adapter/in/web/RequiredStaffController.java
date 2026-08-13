package com.baito.my_app.schedule.adapter.in.web;

import com.baito.my_app.common.security.LoginMember;
import com.baito.my_app.schedule.application.port.in.GetScheduleQuery;
import com.baito.my_app.schedule.application.port.in.SetRequiredStaffUseCase;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
        List<SetRequiredStaffUseCase.Interval> intervals = request.getIntervals().stream()
                .map(i -> new SetRequiredStaffUseCase.Interval(i.getStartTime(), i.getEndTime(), i.getRequiredCount()))
                .toList();
        setRequiredStaffUseCase.setRequiredStaff(new SetRequiredStaffUseCase.Command(
                groupId, loginMember.getMemberId(), request.getWorkDate(), intervals));
    }

    @GetMapping
    public List<RequiredStaffResponse> get(@PathVariable Long groupId,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                           @AuthenticationPrincipal LoginMember loginMember) {
        return getScheduleQuery.getRequiredStaff(groupId, loginMember.getMemberId(), date).stream()
                .map(RequiredStaffResponse::from)
                .toList();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class SetRequiredStaffRequest {
        @NotNull
        private LocalDate workDate;
        // 빈 목록 허용: 하루의 모든 시간대를 비우는(전체 삭제) 저장을 지원한다.
        @NotNull
        @Valid
        private List<IntervalRequest> intervals;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class IntervalRequest {
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;
        @PositiveOrZero
        private int requiredCount;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RequiredStaffResponse {
        private LocalTime startTime;
        private int requiredCount;

        static RequiredStaffResponse from(RequiredStaffSlot slot) {
            return new RequiredStaffResponse(slot.getStartTime(), slot.getRequiredCount());
        }
    }
}
