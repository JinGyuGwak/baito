package com.baito.my_app.assignment.application.service;

import com.baito.my_app.assignment.application.port.in.GetAssignmentsQuery.AssignmentDetail;
import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.member.domain.Role;
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
class AssignmentQueryServiceTest {

    private static final Long GROUP_ID = 10L;
    private static final Long OWNER_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Mock
    private WorkGroupRepository workGroupRepository;
    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private AssignmentQueryService service;

    @Test
    @DisplayName("성공 - 소유자면 해당 날짜의 확정 배정 목록을 회원 이름과 함께 반환한다")
    void getAssignments() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null)));
        given(shiftAssignmentRepository.findConfirmedByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(List.of(
                new ShiftAssignment(1L, GROUP_ID, 2L, DATE, LocalTime.of(9, 0),
                        OWNER_ID, null)));
        given(memberRepository.findAllByIds(List.of(2L))).willReturn(List.of(
                new Member(2L, "worker01", "", "김알바", Role.PART_TIMER, null)));

        assertThat(service.getAssignments(GROUP_ID, OWNER_ID, DATE)).containsExactly(
                new AssignmentDetail(2L, "김알바", "worker01", LocalTime.of(9, 0)));
    }

    @Test
    @DisplayName("성공 - 회원 정보를 못 찾으면 이름/loginId는 null로 채운다")
    void getAssignments_memberMissing() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null)));
        given(shiftAssignmentRepository.findConfirmedByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(List.of(
                new ShiftAssignment(1L, GROUP_ID, 2L, DATE, LocalTime.of(9, 0),
                        OWNER_ID, null)));
        given(memberRepository.findAllByIds(List.of(2L))).willReturn(List.of());

        assertThat(service.getAssignments(GROUP_ID, OWNER_ID, DATE)).containsExactly(
                new AssignmentDetail(2L, null, null, LocalTime.of(9, 0)));
    }

    @Test
    @DisplayName("실패 - 그룹이 없으면 NotGroupOwnerException")
    void groupNotFound() {
        given(workGroupRepository.findById(GROUP_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAssignments(GROUP_ID, OWNER_ID, DATE))
                .isInstanceOf(NotGroupOwnerException.class);
    }

    @Test
    @DisplayName("실패 - 소유자가 아니면 NotGroupOwnerException")
    void notOwner() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, 999L, "강남점", null, null)));

        assertThatThrownBy(() -> service.getAssignments(GROUP_ID, OWNER_ID, DATE))
                .isInstanceOf(NotGroupOwnerException.class);
    }
}
