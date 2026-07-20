package com.baito.my_app.assignment.domain;

import com.baito.my_app.common.exception.DomainException;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Raised when a requested slot falls outside the part-timer's registered availability.
 * Surfaced to the client as an alert (requirement 7).
 */
public class ShiftNotAvailableException extends DomainException {
    public ShiftNotAvailableException(LocalDate workDate, LocalTime startTime) {
        super("아르바이트생의 근무 가능 시간이 아닙니다: " + workDate + " " + startTime);
    }
}
