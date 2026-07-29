package com.baito.my_app.schedule.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface RequiredStaffSlotJpaRepository extends JpaRepository<RequiredStaffSlotJpaEntity, Long> {

    List<RequiredStaffSlotJpaEntity> findByGroupIdAndWorkDate(Long groupId, LocalDate workDate);

    Optional<RequiredStaffSlotJpaEntity> findByGroupIdAndWorkDateAndStartTime(Long groupId, LocalDate workDate, LocalTime startTime);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from RequiredStaffSlotJpaEntity r where r.groupId = :groupId and r.workDate = :workDate")
    void deleteByGroupIdAndWorkDate(Long groupId, LocalDate workDate);
}
