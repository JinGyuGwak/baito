package com.baito.my_app.schedule.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A 30-minute slot in which a part-timer is available to work for a group.
 * Row present = available; absent = unavailable.
 */
@Getter
@Setter
@AllArgsConstructor
public class AvailabilitySlot {

    private Long id;
    private Long groupId;
    private Long memberId;
    private LocalDate workDate;
    private LocalTime startTime;

    public static AvailabilitySlot of(Long groupId, Long memberId, LocalDate workDate, LocalTime startTime) {
        return new AvailabilitySlot(null, groupId, memberId, workDate, startTime);
    }
}
