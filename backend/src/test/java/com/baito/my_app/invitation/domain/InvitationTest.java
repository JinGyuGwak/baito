package com.baito.my_app.invitation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvitationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 1, 9, 0);

    @Test
    @DisplayName("생성 시 PENDING 상태이며 응답 시각은 비어 있다")
    void create() {
        Invitation invitation = Invitation.create(10L, 1L, 2L);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(invitation.isPending()).isTrue();
        assertThat(invitation.getRespondedAt()).isNull();
        assertThat(invitation.getId()).isNull();
    }

    @Test
    @DisplayName("accept 시 ACCEPTED로 전이하고 응답 시각을 기록한다")
    void accept() {
        Invitation invitation = Invitation.create(10L, 1L, 2L);

        invitation.accept(NOW);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(invitation.getRespondedAt()).isEqualTo(NOW);
        assertThat(invitation.isPending()).isFalse();
    }

    @Test
    @DisplayName("reject 시 REJECTED로 전이한다")
    void reject() {
        Invitation invitation = Invitation.create(10L, 1L, 2L);

        invitation.reject(NOW);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.REJECTED);
        assertThat(invitation.getRespondedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("cancel 시 CANCELLED로 전이한다")
    void cancel() {
        Invitation invitation = Invitation.create(10L, 1L, 2L);

        invitation.cancel(NOW);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.CANCELLED);
        assertThat(invitation.getRespondedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("이미 PENDING이 아니면 어떤 전이도 InvalidInvitationStateException")
    void transitionFromNonPending() {
        Invitation accepted = Invitation.create(10L, 1L, 2L);
        accepted.accept(NOW);

        assertThatThrownBy(() -> accepted.reject(NOW))
                .isInstanceOf(InvalidInvitationStateException.class);
        assertThatThrownBy(() -> accepted.cancel(NOW))
                .isInstanceOf(InvalidInvitationStateException.class);
        assertThatThrownBy(() -> accepted.accept(NOW))
                .isInstanceOf(InvalidInvitationStateException.class);
    }
}
