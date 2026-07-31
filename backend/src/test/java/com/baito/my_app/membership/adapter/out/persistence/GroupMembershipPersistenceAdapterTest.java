package com.baito.my_app.membership.adapter.out.persistence;

import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import com.baito.my_app.membership.domain.MembershipStatus;
import com.baito.my_app.support.PersistenceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(GroupMembershipPersistenceAdapter.class)
class GroupMembershipPersistenceAdapterTest extends PersistenceTestSupport {

    private static final Long GROUP_ID = 10L;
    private static final Long MEMBER_ID = 2L;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private GroupMembershipJpaRepository jpaRepository;

    @Test
    @DisplayName("저장 후 (group, member)로 조회되고 활성 여부를 판별한다")
    void save_and_query() {
        membershipRepository.save(GroupMembership.activate(GROUP_ID, MEMBER_ID, LocalDateTime.now()));

        assertThat(membershipRepository.findByGroupIdAndMemberId(GROUP_ID, MEMBER_ID)).isPresent();
        assertThat(membershipRepository.existsActiveMembership(GROUP_ID, MEMBER_ID)).isTrue();
        assertThat(membershipRepository.existsActiveMembership(GROUP_ID, 999L)).isFalse();
    }

    @Test
    @DisplayName("findActiveByMemberId - 회원의 활성 멤버십 목록")
    void findActiveByMemberId() {
        membershipRepository.save(GroupMembership.activate(GROUP_ID, MEMBER_ID, LocalDateTime.now()));
        membershipRepository.save(GroupMembership.activate(20L, MEMBER_ID, LocalDateTime.now()));

        assertThat(membershipRepository.findActiveByMemberId(MEMBER_ID))
                .extracting(GroupMembership::getGroupId)
                .containsExactlyInAnyOrder(GROUP_ID, 20L);
    }

    @Test
    @DisplayName("findActiveByGroupId - 그룹의 활성 멤버십만 반환한다")
    void findActiveByGroupId() {
        membershipRepository.save(GroupMembership.activate(GROUP_ID, MEMBER_ID, LocalDateTime.now()));
        GroupMembership kicked = membershipRepository.save(GroupMembership.activate(GROUP_ID, 3L, LocalDateTime.now()));
        kicked.deactivate();
        membershipRepository.save(kicked);

        assertThat(membershipRepository.findActiveByGroupId(GROUP_ID))
                .extracting(GroupMembership::getMemberId)
                .containsExactly(MEMBER_ID);
    }

    @Test
    @DisplayName("(group, member) UNIQUE 제약 - 같은 조합을 두 번 저장하면 위반")
    void duplicateMembership_violatesUniqueConstraint() {
        membershipRepository.save(GroupMembership.activate(GROUP_ID, MEMBER_ID, LocalDateTime.now()));

        assertThatThrownBy(() -> jpaRepository.saveAndFlush(new GroupMembershipJpaEntity(
                null, GROUP_ID, MEMBER_ID, MembershipStatus.ACTIVE, LocalDateTime.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
