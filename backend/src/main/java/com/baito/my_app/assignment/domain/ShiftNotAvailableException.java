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
        super("アルバイトの勤務可能時間ではありません: " + workDate + " " + startTime);
    }
}
