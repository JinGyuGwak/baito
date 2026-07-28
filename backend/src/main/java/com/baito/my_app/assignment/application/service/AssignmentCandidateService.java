package com.baito.my_app.assignment.application.service;

import com.baito.my_app.assignment.application.port.in.GetAssignmentCandidatesQuery;
import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.common.domain.SlotTimes;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import com.baito.my_app.schedule.application.port.out.AvailabilitySlotRepository;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds the candidate list for a required-staff time block: active members whose availability
 * covers every 30-minute slot of the block. Members already confirmed for the whole block are
 * flagged so the UI can render them unselectable.
 */
@Service
@Transactional(readOnly = true)
public class AssignmentCandidateService implements GetAssignmentCandidatesQuery {

    private final WorkGroupRepository workGroupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final MemberRepository memberRepository;

    public AssignmentCandidateService(WorkGroupRepository workGroupRepository,
                                      GroupMembershipRepository membershipRepository,
                                      AvailabilitySlotRepository availabilitySlotRepository,
                                      ShiftAssignmentRepository shiftAssignmentRepository,
                                      MemberRepository memberRepository) {
        this.workGroupRepository = workGroupRepository;
        this.membershipRepository = membershipRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public List<Candidate> getCandidates(Long groupId, Long ownerId, LocalDate workDate,
                                         LocalTime startTime, LocalTime endTime) {
        WorkGroup group = workGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotGroupOwnerException(groupId));
        if (!group.isOwnedBy(ownerId)) {
            throw new NotGroupOwnerException(groupId);
        }

        List<LocalTime> blockSlots = SlotTimes.expand(startTime, endTime);

        // memberId -> that member's availability slot starts for the day
        Map<Long, Set<LocalTime>> availabilityByMember = new HashMap<>();
        for (AvailabilitySlot slot : availabilitySlotRepository.findByGroupIdAndWorkDate(groupId, workDate)) {
            availabilityByMember.computeIfAbsent(slot.getMemberId(), k -> new HashSet<>()).add(slot.getStartTime());
        }

        // memberId -> that member's confirmed assignment slot starts for the day
        Map<Long, Set<LocalTime>> assignedByMember = new HashMap<>();
        for (ShiftAssignment a : shiftAssignmentRepository.findConfirmedByGroupIdAndWorkDate(groupId, workDate)) {
            assignedByMember.computeIfAbsent(a.getMemberId(), k -> new HashSet<>()).add(a.getStartTime());
        }

        List<Long> availableMemberIds = membershipRepository.findActiveByGroupId(groupId).stream()
                .map(GroupMembership::getMemberId)
                .filter(id -> availabilityByMember.getOrDefault(id, Set.of()).containsAll(blockSlots))
                .toList();

        Map<Long, Member> membersById = memberRepository.findAllByIds(availableMemberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        return availableMemberIds.stream()
                .map(membersById::get)
                .filter(java.util.Objects::nonNull)
                .map(m -> new Candidate(
                        m.getId(), m.getName(), m.getLoginId(),
                        toIntervals(availabilityByMember.get(m.getId())),
                        assignedByMember.getOrDefault(m.getId(), Set.of()).containsAll(blockSlots)))
                .sorted(Comparator.comparing(Candidate::name).thenComparing(Candidate::memberId))
                .toList();
    }

    /** Merges a member's 30-minute availability slots into consecutive [start, end) intervals. */
    private static List<Interval> toIntervals(Set<LocalTime> slotStarts) {
        List<Interval> intervals = new ArrayList<>();
        LocalTime runStart = null;
        LocalTime expected = null;
        for (LocalTime slot : new TreeSet<>(slotStarts)) {
            if (runStart == null || !slot.equals(expected)) {
                if (runStart != null) {
                    intervals.add(new Interval(runStart, expected));
                }
                runStart = slot;
            }
            expected = slot.plusMinutes(SlotTimes.SLOT_MINUTES);
        }
        if (runStart != null) {
            intervals.add(new Interval(runStart, expected));
        }
        return intervals;
    }
}
