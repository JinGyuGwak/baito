package com.baito.my_app.schedule.adapter.out.persistence;

import com.baito.my_app.schedule.application.port.out.RequiredStaffSlotRepository;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
public class RequiredStaffSlotPersistenceAdapter implements RequiredStaffSlotRepository {

    private final RequiredStaffSlotJpaRepository jpaRepository;

    public RequiredStaffSlotPersistenceAdapter(RequiredStaffSlotJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void saveAll(List<RequiredStaffSlot> slots) {
        jpaRepository.saveAll(slots.stream()
                .map(RequiredStaffSlotPersistenceAdapter::toEntity)
                .toList());
    }

    @Override
    public void deleteByGroupIdAndWorkDate(Long groupId, LocalDate workDate) {
        jpaRepository.deleteByGroupIdAndWorkDate(groupId, workDate);
    }

    @Override
    public List<RequiredStaffSlot> findByGroupIdAndWorkDate(Long groupId, LocalDate workDate) {
        return jpaRepository.findByGroupIdAndWorkDate(groupId, workDate).stream()
                .map(RequiredStaffSlotPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<RequiredStaffSlot> findByGroupIdAndWorkDateAndStartTime(Long groupId, LocalDate workDate, LocalTime startTime) {
        return jpaRepository.findByGroupIdAndWorkDateAndStartTime(groupId, workDate, startTime)
                .map(RequiredStaffSlotPersistenceAdapter::toDomain);
    }

    private static RequiredStaffSlotJpaEntity toEntity(RequiredStaffSlot slot) {
        return new RequiredStaffSlotJpaEntity(
                slot.getId(), slot.getGroupId(), slot.getWorkDate(), slot.getStartTime(), slot.getRequiredCount());
    }

    private static RequiredStaffSlot toDomain(RequiredStaffSlotJpaEntity entity) {
        return new RequiredStaffSlot(
                entity.getId(), entity.getGroupId(), entity.getWorkDate(), entity.getStartTime(), entity.getRequiredCount());
    }
}
