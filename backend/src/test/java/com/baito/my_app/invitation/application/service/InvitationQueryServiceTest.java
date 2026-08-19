package com.baito.my_app.invitation.application.service;

import com.baito.my_app.common.domain.PageResult;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.invitation.application.port.in.GetInvitationsQuery.ReceivedInvitation;
import com.baito.my_app.invitation.application.port.in.GetInvitationsQuery.SentInvitation;
import com.baito.my_app.invitation.application.port.out.InvitationRepository;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationStatus;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InvitationQueryServiceTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private WorkGroupRepository workGroupRepository;
    @InjectMocks
    private InvitationQueryService service;

    @Test
    @DisplayName("보낸 초대 - 페이지 결과에 초대받은 회원의 이름/loginId를 붙여 반환한다")
    void getSentInvitations() {
        Invitation invitation = new Invitation(50L, 10L, 1L, 2L,
                InvitationStatus.PENDING, LocalDateTime.now(), null);
        given(invitationRepository.findByInviterIdAndGroupId(1L, 10L, InvitationStatus.PENDING, 0, 10))
                .willReturn(new PageResult<>(List.of(invitation), 0, 10, 1, 1));
        given(memberRepository.findAllByIds(List.of(2L))).willReturn(List.of(
                new Member(2L, "worker01", "", "김알바", Role.PART_TIMER, null)));

        PageResult<SentInvitation> result =
                service.getSentInvitations(1L, 10L, InvitationStatus.PENDING, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(
                new SentInvitation(invitation, "김알바", "worker01"));
    }

    @Test
    @DisplayName("보낸 초대 - 회원 정보를 못 찾으면 이름/loginId는 null")
    void getSentInvitations_memberMissing() {
        Invitation invitation = new Invitation(50L, 10L, 1L, 2L,
                InvitationStatus.PENDING, LocalDateTime.now(), null);
        given(invitationRepository.findByInviterIdAndGroupId(1L, 10L, null, 0, 10))
                .willReturn(new PageResult<>(List.of(invitation), 0, 10, 1, 1));
        given(memberRepository.findAllByIds(List.of(2L))).willReturn(List.of());

        PageResult<SentInvitation> result = service.getSentInvitations(1L, 10L, null, 0, 10);

        assertThat(result.getContent()).containsExactly(new SentInvitation(invitation, null, null));
    }

    @Test
    @DisplayName("받은 초대 - PENDING 초대에 그룹명/초대한 점주 이름을 붙여 반환한다")
    void getReceivedPendingInvitations() {
        Invitation invitation = new Invitation(50L, 10L, 1L, 2L,
                InvitationStatus.PENDING, LocalDateTime.now(), null);
        given(invitationRepository.findPendingByInviteeId(2L)).willReturn(List.of(invitation));
        given(workGroupRepository.findAllByIds(List.of(10L))).willReturn(List.of(
                new WorkGroup(10L, 1L, "강남점", "강남역 1호점", null)));
        given(memberRepository.findAllByIds(List.of(1L))).willReturn(List.of(
                new Member(1L, "owner01", "", "박점주", Role.OWNER, null)));

        List<ReceivedInvitation> result = service.getReceivedPendingInvitations(2L);

        assertThat(result).containsExactly(
                new ReceivedInvitation(invitation, "강남점", "박점주"));
    }

    @Test
    @DisplayName("받은 초대 - 그룹/점주 정보를 못 찾으면 그룹명/점주 이름은 null")
    void getReceivedPendingInvitations_referencesMissing() {
        Invitation invitation = new Invitation(50L, 10L, 1L, 2L,
                InvitationStatus.PENDING, LocalDateTime.now(), null);
        given(invitationRepository.findPendingByInviteeId(2L)).willReturn(List.of(invitation));
        given(workGroupRepository.findAllByIds(List.of(10L))).willReturn(List.of());
        given(memberRepository.findAllByIds(List.of(1L))).willReturn(List.of());

        List<ReceivedInvitation> result = service.getReceivedPendingInvitations(2L);

        assertThat(result).containsExactly(new ReceivedInvitation(invitation, null, null));
    }
}
