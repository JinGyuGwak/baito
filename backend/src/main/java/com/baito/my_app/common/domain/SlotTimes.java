package com.baito.my_app.common.domain;

import com.baito.my_app.common.exception.InvalidSlotTimeException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper for the 30-minute slot model shared by required-staff, availability and assignment.
 * A slot is identified by its start time; slots start on the hour or half hour.
 */
public final class SlotTimes {

    public static final int SLOT_MINUTES = 30;

    private SlotTimes() {
    }

    /**
     * Whether the given time is a valid slot start (aligned to a 30-minute boundary, no seconds/nanos).
     */
    public static boolean isAligned(LocalTime time) {
        return time != null
                && time.getSecond() == 0
                && time.getNano() == 0
                && time.getMinute() % SLOT_MINUTES == 0;
    }

    /**
     * Expands a half-open interval [start, end) into the list of 30-minute slot start times it covers.
     * e.g. 15:00~17:00 -> [15:00, 15:30, 16:00, 16:30].
     *
     * @throws InvalidSlotTimeException if the bounds are not slot-aligned or end is not after start.
     */
    public static List<LocalTime> expand(LocalTime start, LocalTime end) {
        if (!isAligned(start) || !isAligned(end)) {
            throw new InvalidSlotTimeException("時間は30分単位(正時/30分)でなければなりません: " + start + "~" + end);
        }
        if (!end.isAfter(start)) {
            throw new InvalidSlotTimeException("終了時間は開始時間より後でなければなりません: " + start + "~" + end);
        }
        List<LocalTime> slots = new ArrayList<>();
        LocalTime cursor = start;
        while (cursor.isBefore(end)) {
            slots.add(cursor);
            cursor = cursor.plusMinutes(SLOT_MINUTES);
        }
        return slots;
    }
}
