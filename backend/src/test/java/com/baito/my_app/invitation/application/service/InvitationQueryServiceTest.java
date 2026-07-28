package com.baito.my_app.invitation.application.service;

import com.baito.my_app.common.domain.PageResult;
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

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).containsExactly(
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

        assertThat(result.content()).containsExactly(new SentInvitation(invitation, null, null));
    }

    @Test
    @DisplayName("받은 초대 - inviteeId의 PENDING 조회 결과를 그대로 반환한다")
    void getReceivedPendingInvitations() {
        List<Invitation> received = List.of(new Invitation(50L, 10L, 1L, 2L,
                InvitationStatus.PENDING, LocalDateTime.now(), null));
        given(invitationRepository.findPendingByInviteeId(2L)).willReturn(received);

        assertThat(service.getReceivedPendingInvitations(2L)).isEqualTo(received);
    }
}
