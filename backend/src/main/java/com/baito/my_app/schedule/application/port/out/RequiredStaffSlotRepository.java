package com.baito.my_app.schedule.application.port.out;

import com.baito.my_app.schedule.domain.RequiredStaffSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port for required-staff slots.
 */
public interface RequiredStaffSlotRepository {

    void saveAll(List<RequiredStaffSlot> slots);

    void deleteByGroupIdAndWorkDate(Long groupId, LocalDate workDate);

    List<RequiredStaffSlot> findByGroupIdAndWorkDate(Long groupId, LocalDate workDate);

    Optional<RequiredStaffSlot> findByGroupIdAndWorkDateAndStartTime(Long groupId, LocalDate workDate, LocalTime startTime);
}
