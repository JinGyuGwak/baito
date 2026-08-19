package com.baito.my_app.invitation.adapter.out.persistence;

import com.baito.my_app.common.domain.PageResult;
import com.baito.my_app.invitation.application.port.out.InvitationRepository;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationStatus;
import com.baito.my_app.support.PersistenceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(InvitationPersistenceAdapter.class)
class InvitationPersistenceAdapterTest extends PersistenceTestSupport {

    private static final Long GROUP_ID = 10L;
    private static final Long OWNER_ID = 1L;
    private static final Long INVITEE_ID = 2L;

    @Autowired
    private InvitationRepository invitationRepository;

    @Test
    @DisplayName("저장하면 PENDING 상태로 id/createdAt과 함께 조회된다")
    void save_and_findById() {
        Invitation saved = invitationRepository.save(Invitation.create(GROUP_ID, OWNER_ID, INVITEE_ID));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(invitationRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("상태 변경(수락)이 respondedAt과 함께 영속화된다")
    void updateStatus_isPersisted() {
        Invitation saved = invitationRepository.save(Invitation.create(GROUP_ID, OWNER_ID, INVITEE_ID));

        saved.accept(LocalDateTime.of(2026, 5, 2, 10, 0));
        invitationRepository.save(saved);

        assertThat(invitationRepository.findById(saved.getId()))
                .get()
                .satisfies(i -> {
                    assertThat(i.isPending()).isFalse();
                    assertThat(i.getRespondedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("existsPendingByGroupIdAndInviteeId - PENDING 초대만 true")
    void existsPending() {
        invitationRepository.save(Invitation.create(GROUP_ID, OWNER_ID, INVITEE_ID));

        assertThat(invitationRepository.existsPendingByGroupIdAndInviteeId(GROUP_ID, INVITEE_ID)).isTrue();
        assertThat(invitationRepository.existsPendingByGroupIdAndInviteeId(GROUP_ID, 999L)).isFalse();
    }

    @Test
    @DisplayName("보낸/받은 초대 조회 - inviter+group 페이징 / invitee의 PENDING만")
    void findByInviter_and_pendingByInvitee() {
        Invitation pending = invitationRepository.save(Invitation.create(GROUP_ID, OWNER_ID, INVITEE_ID));
        Invitation rejected = invitationRepository.save(Invitation.create(GROUP_ID, OWNER_ID, 3L));
        rejected.reject(LocalDateTime.now());
        invitationRepository.save(rejected);
        invitationRepository.save(Invitation.create(999L, OWNER_ID, INVITEE_ID)); // 다른 그룹 → 제외

        // 상태 필터 없음 → 그룹의 전체 상태
        PageResult<Invitation> all = invitationRepository.findByInviterIdAndGroupId(OWNER_ID, GROUP_ID, null, 0, 10);
        assertThat(all.getTotalElements()).isEqualTo(2);

        // 상태 필터
        PageResult<Invitation> pendingOnly =
                invitationRepository.findByInviterIdAndGroupId(OWNER_ID, GROUP_ID, InvitationStatus.PENDING, 0, 10);
        assertThat(pendingOnly.getContent())
                .extracting(Invitation::getId)
                .containsExactly(pending.getId());

        // 페이징
        PageResult<Invitation> firstPage =
                invitationRepository.findByInviterIdAndGroupId(OWNER_ID, GROUP_ID, null, 0, 1);
        assertThat(firstPage.getContent()).hasSize(1);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);

        assertThat(invitationRepository.findPendingByInviteeId(INVITEE_ID))
                .extracting(Invitation::getId)
                .contains(pending.getId()); // PENDING만
        assertThat(invitationRepository.findPendingByInviteeId(3L)).isEmpty(); // 거절됨
    }
}
