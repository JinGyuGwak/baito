package com.baito.my_app.schedule.application.service;

import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.common.exception.InvalidSlotTimeException;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.schedule.application.port.in.SetRequiredStaffUseCase;
import com.baito.my_app.schedule.application.port.out.RequiredStaffSlotRepository;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequiredStaffServiceTest {

    private static final Long GROUP_ID = 10L;
    private static final Long OWNER_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Mock
    private WorkGroupRepository workGroupRepository;
    @Mock
    private RequiredStaffSlotRepository requiredStaffSlotRepository;
    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @InjectMocks
    private RequiredStaffService service;

    private SetRequiredStaffUseCase.Command command(SetRequiredStaffUseCase.Interval... intervals) {
        return new SetRequiredStaffUseCase.Command(GROUP_ID, OWNER_ID, DATE, List.of(intervals));
    }

    private SetRequiredStaffUseCase.Interval interval(LocalTime start, LocalTime end, int count) {
        return new SetRequiredStaffUseCase.Interval(start, end, count);
    }

    private RequiredStaffSlot slot(LocalTime start, int count) {
        return RequiredStaffSlot.of(GROUP_ID, DATE, start, count);
    }

    private void ownerOwnsGroup() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null)));
    }

    @Test
    @DisplayName("성공 - 구간별 인원을 슬롯으로 전개하고 기존 값을 교체 저장한다")
    void setRequiredStaff() {
        ownerOwnsGroup();

        service.setRequiredStaff(command(interval(LocalTime.of(9, 0), LocalTime.of(10, 0), 2)));

        verify(requiredStaffSlotRepository).deleteByGroupIdAndWorkDate(GROUP_ID, DATE);
        ArgumentCaptor<List<RequiredStaffSlot>> captor = ArgumentCaptor.forClass(List.class);
        verify(requiredStaffSlotRepository).saveAll(captor.capture());
        assertThat(captor.getValue())
                .extracting(RequiredStaffSlot::getStartTime, RequiredStaffSlot::getRequiredCount)
                .containsExactly(
                        tuple(LocalTime.of(9, 0), 2),
                        tuple(LocalTime.of(9, 30), 2));
    }

    @Test
    @DisplayName("성공 - 시간대가 변경되어 사라진 슬롯의 배정만 초기화한다")
    void resetsAssignmentsForRemovedSlots() {
        ownerOwnsGroup();
        // 기존 설정: 09:00~11:00 (09:00, 09:30, 10:00, 10:30)
        given(requiredStaffSlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE))
                .willReturn(List.of(
                        slot(LocalTime.of(9, 0), 2),
                        slot(LocalTime.of(9, 30), 2),
                        slot(LocalTime.of(10, 0), 2),
                        slot(LocalTime.of(10, 30), 2)));

        // 변경: 09:00~10:30 → 10:30 슬롯이 사라진다
        service.setRequiredStaff(command(interval(LocalTime.of(9, 0), LocalTime.of(10, 30), 2)));

        ArgumentCaptor<List<LocalTime>> captor = ArgumentCaptor.forClass(List.class);
        verify(shiftAssignmentRepository)
                .deleteConfirmedInSlotsForAllMembers(eq(GROUP_ID), eq(DATE), captor.capture());
        assertThat(captor.getValue()).containsExactly(LocalTime.of(10, 30));
    }

    @Test
    @DisplayName("성공 - 슬롯이 그대로 유지되면(인원만 변경) 배정을 초기화하지 않는다")
    void keepsAssignmentsWhenSlotsUnchanged() {
        ownerOwnsGroup();
        given(requiredStaffSlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE))
                .willReturn(List.of(
                        slot(LocalTime.of(9, 0), 2),
                        slot(LocalTime.of(9, 30), 2)));

        // 같은 시간대, 인원만 3명으로 변경
        service.setRequiredStaff(command(interval(LocalTime.of(9, 0), LocalTime.of(10, 0), 3)));

        ArgumentCaptor<List<LocalTime>> captor = ArgumentCaptor.forClass(List.class);
        verify(shiftAssignmentRepository)
                .deleteConfirmedInSlotsForAllMembers(eq(GROUP_ID), eq(DATE), captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    @DisplayName("실패 - 그룹 소유자가 아니면 NotGroupOwnerException")
    void notOwner() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, 999L, "강남점", null, null)));

        assertThatThrownBy(() -> service.setRequiredStaff(command(interval(LocalTime.of(9, 0), LocalTime.of(10, 0), 2))))
                .isInstanceOf(NotGroupOwnerException.class);
        verify(requiredStaffSlotRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("실패 - 필요 인원이 음수이면 InvalidSlotTimeException")
    void negativeCount() {
        ownerOwnsGroup();

        assertThatThrownBy(() -> service.setRequiredStaff(command(interval(LocalTime.of(9, 0), LocalTime.of(10, 0), -1))))
                .isInstanceOf(InvalidSlotTimeException.class);
    }

    @Test
    @DisplayName("실패 - 두 구간이 같은 슬롯에서 겹치면 InvalidSlotTimeException")
    void overlappingSlots() {
        ownerOwnsGroup();

        assertThatThrownBy(() -> service.setRequiredStaff(command(
                interval(LocalTime.of(9, 0), LocalTime.of(10, 0), 2),
                interval(LocalTime.of(9, 30), LocalTime.of(10, 30), 3))))
                .isInstanceOf(InvalidSlotTimeException.class);
        verify(requiredStaffSlotRepository, never()).saveAll(anyList());
    }
}
