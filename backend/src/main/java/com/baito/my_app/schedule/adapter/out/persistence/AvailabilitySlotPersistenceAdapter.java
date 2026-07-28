package com.baito.my_app.schedule.adapter.out.persistence;

import com.baito.my_app.schedule.application.port.out.AvailabilitySlotRepository;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class AvailabilitySlotPersistenceAdapter implements AvailabilitySlotRepository {

    private final AvailabilitySlotJpaRepository jpaRepository;

    public AvailabilitySlotPersistenceAdapter(AvailabilitySlotJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void saveAll(List<AvailabilitySlot> slots) {
        jpaRepository.saveAll(slots.stream()
                .map(AvailabilitySlotPersistenceAdapter::toEntity)
                .toList());
    }

    @Override
    public void deleteByGroupIdAndMemberIdAndWorkDate(Long groupId, Long memberId, LocalDate workDate) {
        jpaRepository.deleteByGroupIdAndMemberIdAndWorkDate(groupId, memberId, workDate);
    }

    @Override
    public List<AvailabilitySlot> findByGroupIdAndMemberIdAndWorkDate(Long groupId, Long memberId, LocalDate workDate) {
        return jpaRepository.findByGroupIdAndMemberIdAndWorkDate(groupId, memberId, workDate).stream()
                .map(AvailabilitySlotPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<AvailabilitySlot> findByGroupIdAndWorkDate(Long groupId, LocalDate workDate) {
        return jpaRepository.findByGroupIdAndWorkDate(groupId, workDate).stream()
                .map(AvailabilitySlotPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public boolean existsByGroupIdAndMemberIdAndWorkDateAndStartTime(Long groupId, Long memberId, LocalDate workDate, LocalTime startTime) {
        return jpaRepository.existsByGroupIdAndMemberIdAndWorkDateAndStartTime(groupId, memberId, workDate, startTime);
    }

    private static AvailabilitySlotJpaEntity toEntity(AvailabilitySlot slot) {
        return new AvailabilitySlotJpaEntity(
                slot.getId(), slot.getGroupId(), slot.getMemberId(), slot.getWorkDate(), slot.getStartTime());
    }

    private static AvailabilitySlot toDomain(AvailabilitySlotJpaEntity entity) {
        return new AvailabilitySlot(
                entity.getId(), entity.getGroupId(), entity.getMemberId(), entity.getWorkDate(), entity.getStartTime());
    }
}
