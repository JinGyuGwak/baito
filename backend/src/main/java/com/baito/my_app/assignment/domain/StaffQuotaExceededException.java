package com.baito.my_app.assignment.domain;

import com.baito.my_app.common.exception.DomainException;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Raised when a slot's required headcount is already fully assigned (or no headcount is configured).
 * Surfaced to the client as an alert (requirement 7).
 */
public class StaffQuotaExceededException extends DomainException {
    public StaffQuotaExceededException(LocalDate workDate, LocalTime startTime, int requiredCount) {
        super("この時間帯の必要人数(" + requiredCount + "名)はすでにすべて割り当てられています: "
                + workDate + " " + startTime);
    }
}
