package com.baito.my_app.invitation.application.service;

import com.baito.my_app.invitation.application.port.out.InvitationRepository;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationStatus;
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
    @InjectMocks
    private InvitationQueryService service;

    @Test
    @DisplayName("보낸 초대 - inviterId로 조회 결과를 그대로 반환한다")
    void getSentInvitations() {
        List<Invitation> sent = List.of(new Invitation(50L, 10L, 1L, 2L,
                InvitationStatus.PENDING, LocalDateTime.now(), null));
        given(invitationRepository.findByInviterId(1L)).willReturn(sent);

        assertThat(service.getSentInvitations(1L)).isEqualTo(sent);
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
