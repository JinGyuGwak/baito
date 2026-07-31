package com.baito.my_app.membership.application.service;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.member.domain.Role;
import com.baito.my_app.membership.application.port.in.GetGroupMembersQuery.GroupMember;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MembershipQueryServiceTest {

    private static final Long MEMBER_ID = 2L;

    @Mock
    private GroupMembershipRepository membershipRepository;
    @Mock
    private WorkGroupRepository workGroupRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private MembershipQueryService service;

    @Test
    @DisplayName("활성 멤버십의 그룹들을 조회해 반환한다")
    void getJoinedGroups() {
        given(membershipRepository.findActiveByMemberId(MEMBER_ID)).willReturn(List.of(
                GroupMembership.activate(10L, MEMBER_ID, LocalDateTime.now()),
                GroupMembership.activate(20L, MEMBER_ID, LocalDateTime.now())));
        given(workGroupRepository.findById(10L))
                .willReturn(Optional.of(new WorkGroup(10L, 1L, "강남점", null, null)));
        given(workGroupRepository.findById(20L))
                .willReturn(Optional.of(new WorkGroup(20L, 1L, "홍대점", null, null)));

        assertThat(service.getJoinedGroups(MEMBER_ID))
                .extracting(WorkGroup::getId)
                .containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("멤버십은 있으나 그룹이 조회되지 않으면 결과에서 제외한다")
    void getJoinedGroups_skipsMissingGroup() {
        given(membershipRepository.findActiveByMemberId(MEMBER_ID)).willReturn(List.of(
                GroupMembership.activate(10L, MEMBER_ID, LocalDateTime.now()),
                GroupMembership.activate(20L, MEMBER_ID, LocalDateTime.now())));
        given(workGroupRepository.findById(10L))
                .willReturn(Optional.of(new WorkGroup(10L, 1L, "강남점", null, null)));
        given(workGroupRepository.findById(20L)).willReturn(Optional.empty());

        assertThat(service.getJoinedGroups(MEMBER_ID))
                .extracting(WorkGroup::getId)
                .containsExactly(10L);
    }

    @Test
    @DisplayName("그룹 멤버 목록 - 활성 멤버십을 이름/loginId와 함께 이름순으로 반환한다")
    void getGroupMembers() {
        given(workGroupRepository.findById(10L))
                .willReturn(Optional.of(new WorkGroup(10L, 1L, "강남점", null, null)));
        LocalDateTime joinedAt = LocalDateTime.of(2026, 5, 1, 9, 0);
        given(membershipRepository.findActiveByGroupId(10L)).willReturn(List.of(
                GroupMembership.activate(10L, 2L, joinedAt),
                GroupMembership.activate(10L, 3L, joinedAt)));
        given(memberRepository.findAllByIds(List.of(2L, 3L))).willReturn(List.of(
                new Member(2L, "worker02", "", "나알바", Role.PART_TIMER, null),
                new Member(3L, "worker01", "", "가알바", Role.PART_TIMER, null)));

        assertThat(service.getGroupMembers(10L, 1L)).containsExactly(
                new GroupMember(3L, "가알바", "worker01", joinedAt),
                new GroupMember(2L, "나알바", "worker02", joinedAt));
    }

    @Test
    @DisplayName("그룹 멤버 목록 - 소유자가 아니면 NotGroupOwnerException")
    void getGroupMembers_notOwner() {
        given(workGroupRepository.findById(10L))
                .willReturn(Optional.of(new WorkGroup(10L, 999L, "강남점", null, null)));

        assertThatThrownBy(() -> service.getGroupMembers(10L, 1L))
                .isInstanceOf(NotGroupOwnerException.class);
    }
}
