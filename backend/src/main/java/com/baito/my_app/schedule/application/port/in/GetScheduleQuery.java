package com.baito.my_app.schedule.application.port.in;

import com.baito.my_app.schedule.domain.AvailabilitySlot;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;

import java.time.LocalDate;
import java.util.List;

public interface GetScheduleQuery {

    /** Owner view: required headcount per slot for a group on a date. */
    List<RequiredStaffSlot> getRequiredStaff(Long groupId, Long ownerId, LocalDate workDate);

    /** Part-timer view: their own availability slots for a group on a date. */
    List<AvailabilitySlot> getMyAvailability(Long groupId, Long memberId, LocalDate workDate);
}
