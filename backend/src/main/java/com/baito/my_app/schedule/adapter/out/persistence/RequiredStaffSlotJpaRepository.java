package com.baito.my_app.schedule.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface RequiredStaffSlotJpaRepository extends JpaRepository<RequiredStaffSlotJpaEntity, Long> {

    List<RequiredStaffSlotJpaEntity> findByGroupIdAndWorkDate(Long groupId, LocalDate workDate);

    Optional<RequiredStaffSlotJpaEntity> findByGroupIdAndWorkDateAndStartTime(Long groupId, LocalDate workDate, LocalTime startTime);

    void deleteByGroupIdAndWorkDate(Long groupId, LocalDate workDate);
}
