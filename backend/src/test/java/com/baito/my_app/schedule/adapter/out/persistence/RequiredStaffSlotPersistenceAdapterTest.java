package com.baito.my_app.schedule.adapter.out.persistence;

import com.baito.my_app.schedule.application.port.out.RequiredStaffSlotRepository;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;
import com.baito.my_app.support.PersistenceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(RequiredStaffSlotPersistenceAdapter.class)
class RequiredStaffSlotPersistenceAdapterTest extends PersistenceTestSupport {

    private static final Long GROUP_ID = 10L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Autowired
    private RequiredStaffSlotRepository repository;

    private RequiredStaffSlot slot(LocalTime start, int count) {
        return RequiredStaffSlot.of(GROUP_ID, DATE, start, count);
    }

    @Test
    @DisplayName("saveAll 후 필요 인원 슬롯이 조회되고 필요 인원 값이 보존된다")
    void saveAll_and_find() {
        repository.saveAll(List.of(slot(LocalTime.of(9, 0), 2), slot(LocalTime.of(9, 30), 3)));

        assertThat(repository.findByGroupIdAndWorkDate(GROUP_ID, DATE))
                .extracting(RequiredStaffSlot::getStartTime, RequiredStaffSlot::getRequiredCount)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(LocalTime.of(9, 0), 2),
                        org.assertj.core.groups.Tuple.tuple(LocalTime.of(9, 30), 3));
    }

    @Test
    @DisplayName("findByGroupIdAndWorkDateAndStartTime - 특정 슬롯 단건 조회")
    void findOneSlot() {
        repository.saveAll(List.of(slot(LocalTime.of(9, 0), 2)));

        assertThat(repository.findByGroupIdAndWorkDateAndStartTime(GROUP_ID, DATE, LocalTime.of(9, 0)))
                .get()
                .extracting(RequiredStaffSlot::getRequiredCount)
                .isEqualTo(2);
        assertThat(repository.findByGroupIdAndWorkDateAndStartTime(GROUP_ID, DATE, LocalTime.of(23, 0)))
                .isEmpty();
    }

    @Test
    @DisplayName("deleteByGroupIdAndWorkDate - 그 날짜 슬롯을 모두 삭제한다")
    void deleteByDay() {
        repository.saveAll(List.of(slot(LocalTime.of(9, 0), 2)));

        repository.deleteByGroupIdAndWorkDate(GROUP_ID, DATE);

        assertThat(repository.findByGroupIdAndWorkDate(GROUP_ID, DATE)).isEmpty();
    }
}
