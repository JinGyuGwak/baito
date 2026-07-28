package com.baito.my_app.assignment.adapter.out.persistence;

import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.support.PersistenceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(ShiftAssignmentPersistenceAdapter.class)
class ShiftAssignmentPersistenceAdapterTest extends PersistenceTestSupport {

    private static final Long GROUP_ID = 10L;
    private static final Long MEMBER_ID = 2L;
    private static final Long ASSIGNER_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Autowired
    private ShiftAssignmentRepository repository;

    private ShiftAssignment confirmed(Long memberId, LocalTime start) {
        return ShiftAssignment.confirm(GROUP_ID, memberId, DATE, start, ASSIGNER_ID);
    }

    @Test
    @DisplayName("saveAll 후 확정 배정이 조회되고 createdAt/CONFIRMED 상태가 기록된다")
    void saveAll_and_findConfirmed() {
        repository.saveAll(List.of(confirmed(MEMBER_ID, LocalTime.of(9, 0)), confirmed(MEMBER_ID, LocalTime.of(9, 30))));

        assertThat(repository.findConfirmedByGroupIdAndWorkDate(GROUP_ID, DATE))
                .hasSize(2)
                .allSatisfy(a -> {
                    assertThat(a.getCreatedAt()).isNotNull();
                    assertThat(a.getAssignedBy()).isEqualTo(ASSIGNER_ID);
                });
    }

    @Test
    @DisplayName("countConfirmed - 특정 슬롯의 확정 인원 수")
    void countConfirmed() {
        repository.saveAll(List.of(
                confirmed(2L, LocalTime.of(9, 0)),
                confirmed(3L, LocalTime.of(9, 0)),
                confirmed(2L, LocalTime.of(9, 30))));

        assertThat(repository.countConfirmed(GROUP_ID, DATE, LocalTime.of(9, 0))).isEqualTo(2);
        assertThat(repository.countConfirmed(GROUP_ID, DATE, LocalTime.of(10, 0))).isZero();
    }

    @Test
    @DisplayName("existsConfirmed - 특정 회원이 특정 슬롯에 확정 배정됐는지")
    void existsConfirmed() {
        repository.saveAll(List.of(confirmed(MEMBER_ID, LocalTime.of(9, 0))));

        assertThat(repository.existsConfirmed(GROUP_ID, MEMBER_ID, DATE, LocalTime.of(9, 0))).isTrue();
        assertThat(repository.existsConfirmed(GROUP_ID, 999L, DATE, LocalTime.of(9, 0))).isFalse();
    }

    @Test
    @DisplayName("deleteConfirmedInSlots - 지정 슬롯의 확정 배정만 삭제하고 삭제된 개수를 반환한다")
    void deleteConfirmedInSlots() {
        repository.saveAll(List.of(
                confirmed(MEMBER_ID, LocalTime.of(9, 0)),
                confirmed(MEMBER_ID, LocalTime.of(9, 30)),
                confirmed(MEMBER_ID, LocalTime.of(11, 0)))); // 범위 밖

        int deleted = repository.deleteConfirmedInSlots(
                GROUP_ID, MEMBER_ID, DATE, List.of(LocalTime.of(9, 0), LocalTime.of(9, 30)));

        assertThat(deleted).isEqualTo(2);
        assertThat(repository.countConfirmed(GROUP_ID, DATE, LocalTime.of(9, 0))).isZero();
        assertThat(repository.countConfirmed(GROUP_ID, DATE, LocalTime.of(9, 30))).isZero();
        assertThat(repository.countConfirmed(GROUP_ID, DATE, LocalTime.of(11, 0))).isEqualTo(1); // 범위 밖은 유지
    }
}
