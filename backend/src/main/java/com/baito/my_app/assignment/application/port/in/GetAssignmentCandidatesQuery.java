package com.baito.my_app.assignment.application.port.in;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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
    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode
    class Candidate {
        private Long memberId;
        private String name;
        private String loginId;
        private List<Interval> availableIntervals;
        private boolean alreadyAssigned;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @EqualsAndHashCode
    class Interval {
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
