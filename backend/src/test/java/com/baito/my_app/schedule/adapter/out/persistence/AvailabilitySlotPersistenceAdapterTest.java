package com.baito.my_app.schedule.adapter.out.persistence;

import com.baito.my_app.schedule.application.port.out.AvailabilitySlotRepository;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
import com.baito.my_app.support.PersistenceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(AvailabilitySlotPersistenceAdapter.class)
class AvailabilitySlotPersistenceAdapterTest extends PersistenceTestSupport {

    private static final Long GROUP_ID = 10L;
    private static final Long MEMBER_ID = 2L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Autowired
    private AvailabilitySlotRepository repository;

    private AvailabilitySlot slot(LocalTime start) {
        return AvailabilitySlot.of(GROUP_ID, MEMBER_ID, DATE, start);
    }

    @Test
    @DisplayName("saveAll 후 해당 (group, member, date)의 슬롯이 조회된다")
    void saveAll_and_find() {
        repository.saveAll(List.of(slot(LocalTime.of(9, 0)), slot(LocalTime.of(9, 30))));

        assertThat(repository.findByGroupIdAndMemberIdAndWorkDate(GROUP_ID, MEMBER_ID, DATE))
                .extracting(AvailabilitySlot::getStartTime)
                .containsExactlyInAnyOrder(LocalTime.of(9, 0), LocalTime.of(9, 30));
    }

    @Test
    @DisplayName("existsByGroupIdAndMemberIdAndWorkDateAndStartTime - 특정 슬롯 존재 여부")
    void existsSlot() {
        repository.saveAll(List.of(slot(LocalTime.of(9, 0))));

        assertThat(repository.existsByGroupIdAndMemberIdAndWorkDateAndStartTime(
                GROUP_ID, MEMBER_ID, DATE, LocalTime.of(9, 0))).isTrue();
        assertThat(repository.existsByGroupIdAndMemberIdAndWorkDateAndStartTime(
                GROUP_ID, MEMBER_ID, DATE, LocalTime.of(23, 0))).isFalse();
    }

    @Test
    @DisplayName("findByGroupIdAndWorkDate - 그룹의 모든 멤버 슬롯을 반환한다")
    void findByGroupAndDate() {
        repository.saveAll(List.of(
                slot(LocalTime.of(9, 0)),
                AvailabilitySlot.of(GROUP_ID, 3L, DATE, LocalTime.of(10, 0)),
                AvailabilitySlot.of(999L, MEMBER_ID, DATE, LocalTime.of(9, 0)))); // 다른 그룹 → 제외

        assertThat(repository.findByGroupIdAndWorkDate(GROUP_ID, DATE))
                .extracting(AvailabilitySlot::getMemberId)
                .containsExactlyInAnyOrder(MEMBER_ID, 3L);
    }

    @Test
    @DisplayName("deleteByGroupIdAndMemberIdAndWorkDate - 그 날짜의 슬롯만 삭제한다")
    void deleteByDay() {
        repository.saveAll(List.of(slot(LocalTime.of(9, 0)), slot(LocalTime.of(9, 30))));

        repository.deleteByGroupIdAndMemberIdAndWorkDate(GROUP_ID, MEMBER_ID, DATE);

        assertThat(repository.findByGroupIdAndMemberIdAndWorkDate(GROUP_ID, MEMBER_ID, DATE)).isEmpty();
    }
}
