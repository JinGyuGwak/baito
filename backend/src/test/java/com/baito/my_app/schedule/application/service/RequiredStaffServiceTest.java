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
    @DisplayName("성공 - 합쳐진 필요인원 블록 안 슬롯이 사라지면 블록 전체의 배정을 취소한다")
    void cancelsWholeMergedBlockContainingRemovedSlot() {
        ownerOwnsGroup();
        // 기존 설정: 09:00~13:00 이 하나의 연속 블록(09:00 ~ 12:30 슬롯). 배정은 09:00~12:00 구간에만 있다.
        given(requiredStaffSlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE))
                .willReturn(List.of(
                        slot(LocalTime.of(9, 0), 3), slot(LocalTime.of(9, 30), 3),
                        slot(LocalTime.of(10, 0), 3), slot(LocalTime.of(10, 30), 3),
                        slot(LocalTime.of(11, 0), 3), slot(LocalTime.of(11, 30), 3),
                        slot(LocalTime.of(12, 0), 3), slot(LocalTime.of(12, 30), 3)));

        // 변경: 12:00~13:00 을 비운다 → 12:00, 12:30 슬롯이 사라진다.
        service.setRequiredStaff(command(interval(LocalTime.of(9, 0), LocalTime.of(12, 0), 3)));

        // 사라진 슬롯이 걸친 블록(09:00~13:00) 전체가 모든 회원 기준으로 취소되어야 한다.
        ArgumentCaptor<List<LocalTime>> captor = ArgumentCaptor.forClass(List.class);
        verify(shiftAssignmentRepository)
                .deleteConfirmedInSlotsForAllMembers(eq(GROUP_ID), eq(DATE), captor.capture());
        assertThat(captor.getValue()).containsExactly(
                LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(11, 0), LocalTime.of(11, 30), LocalTime.of(12, 0), LocalTime.of(12, 30));
    }

    @Test
    @DisplayName("성공 - 인접 시간대가 추가되어 블록이 커지면(병합) 기존 블록의 배정을 취소한다")
    void cancelsWhenBlockGrowsByMerge() {
        ownerOwnsGroup();
        // 기존 설정: 09:00~12:00 (09:00 ~ 11:30 슬롯). 배정은 이 구간에만 있다.
        given(requiredStaffSlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE))
                .willReturn(List.of(
                        slot(LocalTime.of(9, 0), 1), slot(LocalTime.of(9, 30), 1),
                        slot(LocalTime.of(10, 0), 1), slot(LocalTime.of(10, 30), 1),
                        slot(LocalTime.of(11, 0), 1), slot(LocalTime.of(11, 30), 1)));

        // 변경: 12:00~13:00 을 붙여 09:00~13:00 하나의 블록으로 커진다(사라진 슬롯은 없음).
        service.setRequiredStaff(command(interval(LocalTime.of(9, 0), LocalTime.of(13, 0), 1)));

        // 커진 블록의 기존 슬롯(09:00~11:30)에 있던 배정이 모두 취소되어야 한다.
        ArgumentCaptor<List<LocalTime>> captor = ArgumentCaptor.forClass(List.class);
        verify(shiftAssignmentRepository)
                .deleteConfirmedInSlotsForAllMembers(eq(GROUP_ID), eq(DATE), captor.capture());
        assertThat(captor.getValue()).containsExactly(
                LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0),
                LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    @DisplayName("성공 - 사라진 슬롯과 떨어진 다른 연속 블록의 배정은 유지한다")
    void keepsUnaffectedBlocks() {
        ownerOwnsGroup();
        // 서로 떨어진 두 블록: 09:00~10:00, 14:00~15:00
        given(requiredStaffSlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE))
                .willReturn(List.of(
                        slot(LocalTime.of(9, 0), 1), slot(LocalTime.of(9, 30), 1),
                        slot(LocalTime.of(14, 0), 1), slot(LocalTime.of(14, 30), 1)));

        // 변경: 09:00 블록만 비운다 → 09:00, 09:30 슬롯이 사라진다.
        service.setRequiredStaff(command(interval(LocalTime.of(14, 0), LocalTime.of(15, 0), 1)));

        // 사라진 블록만 취소되고 14:00 블록은 취소 대상에 포함되지 않는다.
        ArgumentCaptor<List<LocalTime>> captor = ArgumentCaptor.forClass(List.class);
        verify(shiftAssignmentRepository)
                .deleteConfirmedInSlotsForAllMembers(eq(GROUP_ID), eq(DATE), captor.capture());
        assertThat(captor.getValue()).containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 30));
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

        // 사라진 슬롯이 없으므로 배정 삭제를 아예 시도하지 않는다.
        verify(shiftAssignmentRepository, never())
                .deleteConfirmedInSlotsForAllMembers(eq(GROUP_ID), eq(DATE), anyList());
    }

    @Test
    @DisplayName("성공 - 빈 구간 목록이면 하루 전체를 비우고 배정도 전부 취소한다")
    void clearsWholeDayWithEmptyIntervals() {
        ownerOwnsGroup();
        // 기존 설정: 09:00~10:00 (09:00, 09:30)
        given(requiredStaffSlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE))
                .willReturn(List.of(slot(LocalTime.of(9, 0), 2), slot(LocalTime.of(9, 30), 2)));

        // 빈 구간 목록으로 저장 → 하루 전체 삭제
        service.setRequiredStaff(command());

        verify(requiredStaffSlotRepository).deleteByGroupIdAndWorkDate(GROUP_ID, DATE);
        ArgumentCaptor<List<LocalTime>> cancelCaptor = ArgumentCaptor.forClass(List.class);
        verify(shiftAssignmentRepository)
                .deleteConfirmedInSlotsForAllMembers(eq(GROUP_ID), eq(DATE), cancelCaptor.capture());
        assertThat(cancelCaptor.getValue()).containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 30));
        ArgumentCaptor<List<RequiredStaffSlot>> captor = ArgumentCaptor.forClass(List.class);
        verify(requiredStaffSlotRepository).saveAll(captor.capture());
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
