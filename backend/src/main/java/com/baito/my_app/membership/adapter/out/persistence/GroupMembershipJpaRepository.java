package com.baito.my_app.membership.adapter.out.persistence;

import com.baito.my_app.membership.domain.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMembershipJpaRepository extends JpaRepository<GroupMembershipJpaEntity, Long> {

    Optional<GroupMembershipJpaEntity> findByGroupIdAndMemberId(Long groupId, Long memberId);

    boolean existsByGroupIdAndMemberIdAndStatus(Long groupId, Long memberId, MembershipStatus status);

    List<GroupMembershipJpaEntity> findByMemberIdAndStatus(Long memberId, MembershipStatus status);

    List<GroupMembershipJpaEntity> findByGroupIdAndStatus(Long groupId, MembershipStatus status);
}
