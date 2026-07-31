package com.baito.my_app.assignment.adapter.out.persistence;

import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class ShiftAssignmentPersistenceAdapter implements ShiftAssignmentRepository {

    private final ShiftAssignmentJpaRepository jpaRepository;

    public ShiftAssignmentPersistenceAdapter(ShiftAssignmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void saveAll(List<ShiftAssignment> assignments) {
        jpaRepository.saveAll(assignments.stream()
                .map(ShiftAssignmentPersistenceAdapter::toEntity)
                .toList());
    }

    @Override
    public int countConfirmed(Long groupId, LocalDate workDate, LocalTime startTime) {
        return jpaRepository.countByGroupIdAndWorkDateAndStartTime(groupId, workDate, startTime);
    }

    @Override
    public boolean existsConfirmed(Long groupId, Long memberId, LocalDate workDate, LocalTime startTime) {
        return jpaRepository.existsByGroupIdAndMemberIdAndWorkDateAndStartTime(groupId, memberId, workDate, startTime);
    }

    @Override
    public List<ShiftAssignment> findConfirmedByGroupIdAndWorkDate(Long groupId, LocalDate workDate) {
        return jpaRepository.findByGroupIdAndWorkDate(groupId, workDate).stream()
                .map(ShiftAssignmentPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<ShiftAssignment> findConfirmedByMemberIdAndWorkDate(Long memberId, LocalDate workDate) {
        return jpaRepository.findByMemberIdAndWorkDate(memberId, workDate).stream()
                .map(ShiftAssignmentPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public int deleteConfirmedInSlots(Long groupId, Long memberId, LocalDate workDate, List<LocalTime> startTimes) {
        return jpaRepository.deleteInSlots(groupId, memberId, workDate, startTimes);
    }

    @Override
    public int deleteConfirmedInSlotsForAllMembers(Long groupId, LocalDate workDate, List<LocalTime> startTimes) {
        if (startTimes.isEmpty()) {
            return 0;
        }
        return jpaRepository.deleteInSlotsForAllMembers(groupId, workDate, startTimes);
    }

    private static ShiftAssignmentJpaEntity toEntity(ShiftAssignment a) {
        return new ShiftAssignmentJpaEntity(
                a.getId(), a.getGroupId(), a.getMemberId(), a.getWorkDate(), a.getStartTime(),
                a.getAssignedBy(), a.getCreatedAt());
    }

    private static ShiftAssignment toDomain(ShiftAssignmentJpaEntity e) {
        return new ShiftAssignment(
                e.getId(), e.getGroupId(), e.getMemberId(), e.getWorkDate(), e.getStartTime(),
                e.getAssignedBy(), e.getCreatedAt());
    }
}
