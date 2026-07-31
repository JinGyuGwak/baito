package com.baito.my_app.assignment.application.service;

import com.baito.my_app.assignment.application.port.in.GetAssignmentCandidatesQuery.Candidate;
import com.baito.my_app.assignment.application.port.in.GetAssignmentCandidatesQuery.Interval;
import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.member.domain.Role;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import com.baito.my_app.schedule.application.port.out.AvailabilitySlotRepository;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AssignmentCandidateServiceTest {

    private static final Long GROUP_ID = 10L;
    private static final Long OWNER_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Mock
    private WorkGroupRepository workGroupRepository;
    @Mock
    private GroupMembershipRepository membershipRepository;
    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;
    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private AssignmentCandidateService service;

    private void ownGroup() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null)));
    }

    @Test
    @DisplayName("선택 시간대의 모든 슬롯을 커버하는 활성 멤버만 후보로 반환하고, 이름순 정렬한다")
    void getCandidates_filtersByAvailabilityCoverage() {
        ownGroup();
        given(membershipRepository.findActiveByGroupId(GROUP_ID)).willReturn(List.of(
                GroupMembership.activate(GROUP_ID, 2L, null),
                GroupMembership.activate(GROUP_ID, 3L, null),
                GroupMembership.activate(GROUP_ID, 4L, null)));
        // member 2: 09:00~11:00 커버, member 3: 09:00 슬롯만 (부분 커버 → 제외), member 4: 없음
        given(availabilitySlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(List.of(
                AvailabilitySlot.of(GROUP_ID, 2L, DATE, LocalTime.of(9, 0)),
                AvailabilitySlot.of(GROUP_ID, 2L, DATE, LocalTime.of(9, 30)),
                AvailabilitySlot.of(GROUP_ID, 3L, DATE, LocalTime.of(9, 0))));
        given(shiftAssignmentRepository.findConfirmedByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(List.of());
        given(memberRepository.findAllByIds(List.of(2L))).willReturn(List.of(
                new Member(2L, "worker01", "", "김알바", Role.PART_TIMER, null)));

        List<Candidate> candidates =
                service.getCandidates(GROUP_ID, OWNER_ID, DATE, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThat(candidates).hasSize(1);
        Candidate c = candidates.get(0);
        assertThat(c.memberId()).isEqualTo(2L);
        assertThat(c.name()).isEqualTo("김알바");
        assertThat(c.loginId()).isEqualTo("worker01");
        assertThat(c.alreadyAssigned()).isFalse();
        assertThat(c.availableIntervals())
                .containsExactly(new Interval(LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("이미 시간대 전체에 배정된 멤버는 alreadyAssigned=true로 표시된다")
    void getCandidates_flagsAlreadyAssigned() {
        ownGroup();
        given(membershipRepository.findActiveByGroupId(GROUP_ID)).willReturn(List.of(
                GroupMembership.activate(GROUP_ID, 2L, null)));
        given(availabilitySlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(List.of(
                AvailabilitySlot.of(GROUP_ID, 2L, DATE, LocalTime.of(9, 0)),
                AvailabilitySlot.of(GROUP_ID, 2L, DATE, LocalTime.of(9, 30))));
        given(shiftAssignmentRepository.findConfirmedByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(List.of(
                ShiftAssignment.confirm(GROUP_ID, 2L, DATE, LocalTime.of(9, 0), OWNER_ID),
                ShiftAssignment.confirm(GROUP_ID, 2L, DATE, LocalTime.of(9, 30), OWNER_ID)));
        given(memberRepository.findAllByIds(List.of(2L))).willReturn(List.of(
                new Member(2L, "worker01", "", "김알바", Role.PART_TIMER, null)));

        List<Candidate> candidates =
                service.getCandidates(GROUP_ID, OWNER_ID, DATE, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThat(candidates).singleElement()
                .satisfies(c -> assertThat(c.alreadyAssigned()).isTrue());
    }

    @Test
    @DisplayName("부분 배정(일부 슬롯만 확정)된 멤버는 여전히 선택 가능(alreadyAssigned=false)")
    void getCandidates_partialAssignmentStillSelectable() {
        ownGroup();
        given(membershipRepository.findActiveByGroupId(GROUP_ID)).willReturn(List.of(
                GroupMembership.activate(GROUP_ID, 2L, null)));
        given(availabilitySlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(List.of(
                AvailabilitySlot.of(GROUP_ID, 2L, DATE, LocalTime.of(9, 0)),
                AvailabilitySlot.of(GROUP_ID, 2L, DATE, LocalTime.of(9, 30))));
        given(shiftAssignmentRepository.findConfirmedByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(List.of(
                ShiftAssignment.confirm(GROUP_ID, 2L, DATE, LocalTime.of(9, 0), OWNER_ID)));
        given(memberRepository.findAllByIds(List.of(2L))).willReturn(List.of(
                new Member(2L, "worker01", "", "김알바", Role.PART_TIMER, null)));

        List<Candidate> candidates =
                service.getCandidates(GROUP_ID, OWNER_ID, DATE, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThat(candidates).singleElement()
                .satisfies(c -> assertThat(c.alreadyAssigned()).isFalse());
    }

    @Test
    @DisplayName("불연속 가능 슬롯은 여러 구간으로 병합되어 반환된다")
    void getCandidates_mergesIntervals() {
        ownGroup();
        given(membershipRepository.findActiveByGroupId(GROUP_ID)).willReturn(List.of(
                GroupMembership.activate(GROUP_ID, 2L, null)));
        given(availabilitySlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(List.of(
                AvailabilitySlot.of(GROUP_ID, 2L, DATE, LocalTime.of(9, 0)),
                AvailabilitySlot.of(GROUP_ID, 2L, DATE, LocalTime.of(9, 30)),
                AvailabilitySlot.of(GROUP_ID, 2L, DATE, LocalTime.of(14, 0))));
        given(shiftAssignmentRepository.findConfirmedByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(List.of());
        given(memberRepository.findAllByIds(List.of(2L))).willReturn(List.of(
                new Member(2L, "worker01", "", "김알바", Role.PART_TIMER, null)));

        List<Candidate> candidates =
                service.getCandidates(GROUP_ID, OWNER_ID, DATE, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertThat(candidates.get(0).availableIntervals()).containsExactly(
                new Interval(LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new Interval(LocalTime.of(14, 0), LocalTime.of(14, 30)));
    }

    @Test
    @DisplayName("실패 - 소유자가 아니면 NotGroupOwnerException")
    void notOwner() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, 999L, "강남점", null, null)));

        assertThatThrownBy(() ->
                service.getCandidates(GROUP_ID, OWNER_ID, DATE, LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .isInstanceOf(NotGroupOwnerException.class);
    }
}
