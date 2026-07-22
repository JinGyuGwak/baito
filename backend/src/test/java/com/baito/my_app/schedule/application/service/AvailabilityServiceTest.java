package com.baito.my_app.schedule.application.service;

import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.NotGroupMemberException;
import com.baito.my_app.schedule.application.port.in.SetAvailabilityUseCase;
import com.baito.my_app.schedule.application.port.out.AvailabilitySlotRepository;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    private static final Long GROUP_ID = 10L;
    private static final Long MEMBER_ID = 2L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Mock
    private GroupMembershipRepository membershipRepository;
    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;
    @InjectMocks
    private AvailabilityService service;

    private SetAvailabilityUseCase.Command command(SetAvailabilityUseCase.Interval... intervals) {
        return new SetAvailabilityUseCase.Command(GROUP_ID, MEMBER_ID, DATE, List.of(intervals));
    }

    private SetAvailabilityUseCase.Interval interval(LocalTime start, LocalTime end) {
        return new SetAvailabilityUseCase.Interval(start, end);
    }

    @Test
    @DisplayName("성공 - 구간을 30분 슬롯으로 전개하고, 기존 값을 삭제한 뒤 저장한다")
    void setAvailability() {
        given(membershipRepository.existsActiveMembership(GROUP_ID, MEMBER_ID)).willReturn(true);

        service.setAvailability(command(interval(LocalTime.of(9, 0), LocalTime.of(10, 30))));

        // delete 후 saveAll 순서 보장
        InOrder order = inOrder(availabilitySlotRepository);
        order.verify(availabilitySlotRepository).deleteByGroupIdAndMemberIdAndWorkDate(GROUP_ID, MEMBER_ID, DATE);
        ArgumentCaptor<List<AvailabilitySlot>> captor = ArgumentCaptor.forClass(List.class);
        order.verify(availabilitySlotRepository).saveAll(captor.capture());

        assertThat(captor.getValue()).extracting(AvailabilitySlot::getStartTime)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("성공 - 겹치는 구간의 슬롯은 중복 없이 합집합 처리된다")
    void setAvailability_overlappingIntervalsUnion() {
        given(membershipRepository.existsActiveMembership(GROUP_ID, MEMBER_ID)).willReturn(true);

        service.setAvailability(command(
                interval(LocalTime.of(9, 0), LocalTime.of(10, 0)),
                interval(LocalTime.of(9, 30), LocalTime.of(10, 30))));

        ArgumentCaptor<List<AvailabilitySlot>> captor = ArgumentCaptor.forClass(List.class);
        verify(availabilitySlotRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(AvailabilitySlot::getStartTime)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("실패 - 활성 멤버가 아니면 NotGroupMemberException, 아무것도 저장하지 않는다")
    void setAvailability_notMember() {
        given(membershipRepository.existsActiveMembership(GROUP_ID, MEMBER_ID)).willReturn(false);

        assertThatThrownBy(() -> service.setAvailability(command(interval(LocalTime.of(9, 0), LocalTime.of(10, 0)))))
                .isInstanceOf(NotGroupMemberException.class);

        verify(availabilitySlotRepository, never()).deleteByGroupIdAndMemberIdAndWorkDate(any(), any(), any());
        verify(availabilitySlotRepository, never()).saveAll(anyList());
    }
}
