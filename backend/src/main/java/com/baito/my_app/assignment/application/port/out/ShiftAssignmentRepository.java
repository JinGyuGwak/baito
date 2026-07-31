package com.baito.my_app.assignment.application.port.out;

import com.baito.my_app.assignment.domain.ShiftAssignment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Outbound port for shift assignments.
 */
public interface ShiftAssignmentRepository {

    void saveAll(List<ShiftAssignment> assignments);

    /** Number of CONFIRMED assignments for a slot — compared against the required headcount. */
    int countConfirmed(Long groupId, LocalDate workDate, LocalTime startTime);

    /** Whether this member already has a CONFIRMED assignment for the slot. */
    boolean existsConfirmed(Long groupId, Long memberId, LocalDate workDate, LocalTime startTime);

    List<ShiftAssignment> findConfirmedByGroupIdAndWorkDate(Long groupId, LocalDate workDate);

    /** A member's CONFIRMED assignments on a date, across all groups. */
    List<ShiftAssignment> findConfirmedByMemberIdAndWorkDate(Long memberId, LocalDate workDate);

    /**
     * Deletes a member's CONFIRMED assignments for the given slot start times on a date.
     *
     * @return the number of rows deleted
     */
    int deleteConfirmedInSlots(Long groupId, Long memberId, LocalDate workDate, List<LocalTime> startTimes);

    /**
     * Deletes every member's CONFIRMED assignments for the given slot start times on a date.
     * Used to keep assignments consistent when the required-staff configuration removes/changes a slot.
     *
     * @return the number of rows deleted
     */
    int deleteConfirmedInSlotsForAllMembers(Long groupId, LocalDate workDate, List<LocalTime> startTimes);
}
