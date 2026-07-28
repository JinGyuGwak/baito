package com.baito.my_app.assignment.application.service;

import com.baito.my_app.assignment.application.port.in.CancelShiftUseCase;
import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CancelShiftServiceTest {

    private static final Long GROUP_ID = 10L;
    private static final Long OWNER_ID = 1L;
    private static final Long MEMBER_ID = 2L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Mock
    private WorkGroupRepository workGroupRepository;
    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @InjectMocks
    private CancelShiftService service;

    private CancelShiftUseCase.Command command(LocalTime start, LocalTime end) {
        return new CancelShiftUseCase.Command(GROUP_ID, OWNER_ID, MEMBER_ID, DATE, start, end);
    }

    @Test
    @DisplayName("성공 - 범위 내 확정 슬롯을 삭제하고 삭제된 개수를 반환한다")
    void cancel() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null)));
        given(shiftAssignmentRepository.deleteConfirmedInSlots(
                GROUP_ID, MEMBER_ID, DATE, List.of(LocalTime.of(9, 0), LocalTime.of(9, 30))))
                .willReturn(2);

        int cancelled = service.cancel(command(LocalTime.of(9, 0), LocalTime.of(10, 0)));

        assertThat(cancelled).isEqualTo(2);
    }

    @Test
    @DisplayName("성공 - 확정 배정이 없으면 0을 반환한다")
    void cancel_nothingConfirmed() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null)));
        given(shiftAssignmentRepository.deleteConfirmedInSlots(GROUP_ID, MEMBER_ID, DATE,
                List.of(LocalTime.of(9, 0)))).willReturn(0);

        assertThat(service.cancel(command(LocalTime.of(9, 0), LocalTime.of(9, 30)))).isZero();
    }

    @Test
    @DisplayName("실패 - 소유자가 아니면 NotGroupOwnerException")
    void notOwner() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, 999L, "강남점", null, null)));

        assertThatThrownBy(() -> service.cancel(command(LocalTime.of(9, 0), LocalTime.of(9, 30))))
                .isInstanceOf(NotGroupOwnerException.class);
        then(shiftAssignmentRepository).shouldHaveNoInteractions();
    }
}
