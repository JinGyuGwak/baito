package com.baito.my_app.assignment.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Owner picks a required-staff time block and sees which part-timers can work it.
 * A candidate is an active group member whose availability covers every 30-minute slot of the
 * block; {@code alreadyAssigned} marks members already confirmed for the whole block so the UI
 * can render them as unselectable.
 */
public interface GetAssignmentCandidatesQuery {

    List<Candidate> getCandidates(Long groupId, Long ownerId, LocalDate workDate,
                                  LocalTime startTime, LocalTime endTime);

    /**
     * @param availableIntervals the member's merged availability ranges for the whole day,
     *                           shown as "근무 가능 시간대" in the UI
     */
    record Candidate(Long memberId, String name, String loginId,
                     List<Interval> availableIntervals, boolean alreadyAssigned) {
    }

    record Interval(LocalTime startTime, LocalTime endTime) {
    }
}
