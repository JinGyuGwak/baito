package com.baito.my_app.schedule.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AvailabilitySlotJpaRepository extends JpaRepository<AvailabilitySlotJpaEntity, Long> {

    List<AvailabilitySlotJpaEntity> findByGroupIdAndMemberIdAndWorkDate(Long groupId, Long memberId, LocalDate workDate);

    List<AvailabilitySlotJpaEntity> findByGroupIdAndWorkDate(Long groupId, LocalDate workDate);

    boolean existsByGroupIdAndMemberIdAndWorkDateAndStartTime(Long groupId, Long memberId, LocalDate workDate, LocalTime startTime);

    void deleteByGroupIdAndMemberIdAndWorkDate(Long groupId, Long memberId, LocalDate workDate);
}
