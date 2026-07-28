package com.baito.my_app.assignment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ShiftAssignmentJpaRepository extends JpaRepository<ShiftAssignmentJpaEntity, Long> {

    int countByGroupIdAndWorkDateAndStartTime(Long groupId, LocalDate workDate, LocalTime startTime);

    boolean existsByGroupIdAndMemberIdAndWorkDateAndStartTime(Long groupId, Long memberId, LocalDate workDate,
                                                              LocalTime startTime);

    List<ShiftAssignmentJpaEntity> findByGroupIdAndWorkDate(Long groupId, LocalDate workDate);

    List<ShiftAssignmentJpaEntity> findByMemberIdAndWorkDate(Long memberId, LocalDate workDate);

    @Modifying
    @Query("delete from ShiftAssignmentJpaEntity a where a.groupId = :groupId and a.memberId = :memberId "
            + "and a.workDate = :workDate and a.startTime in :startTimes")
    int deleteInSlots(@Param("groupId") Long groupId, @Param("memberId") Long memberId,
                      @Param("workDate") LocalDate workDate, @Param("startTimes") List<LocalTime> startTimes);
}
