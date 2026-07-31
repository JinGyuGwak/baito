package com.baito.my_app.membership.adapter.out.persistence;

import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import com.baito.my_app.membership.domain.MembershipStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GroupMembershipPersistenceAdapter implements GroupMembershipRepository {

    private final GroupMembershipJpaRepository jpaRepository;

    public GroupMembershipPersistenceAdapter(GroupMembershipJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GroupMembership save(GroupMembership membership) {
        return toDomain(jpaRepository.save(toEntity(membership)));
    }

    @Override
    public Optional<GroupMembership> findByGroupIdAndMemberId(Long groupId, Long memberId) {
        return jpaRepository.findByGroupIdAndMemberId(groupId, memberId)
                .map(GroupMembershipPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsActiveMembership(Long groupId, Long memberId) {
        return jpaRepository.existsByGroupIdAndMemberIdAndStatus(groupId, memberId, MembershipStatus.ACTIVE);
    }

    @Override
    public List<GroupMembership> findActiveByMemberId(Long memberId) {
        return jpaRepository.findByMemberIdAndStatus(memberId, MembershipStatus.ACTIVE).stream()
                .map(GroupMembershipPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<GroupMembership> findActiveByGroupId(Long groupId) {
        return jpaRepository.findByGroupIdAndStatus(groupId, MembershipStatus.ACTIVE).stream()
                .map(GroupMembershipPersistenceAdapter::toDomain)
                .toList();
    }

    private static GroupMembershipJpaEntity toEntity(GroupMembership membership) {
        return new GroupMembershipJpaEntity(
                membership.getId(),
                membership.getGroupId(),
                membership.getMemberId(),
                membership.getStatus(),
                membership.getJoinedAt()
        );
    }

    private static GroupMembership toDomain(GroupMembershipJpaEntity entity) {
        return new GroupMembership(
                entity.getId(),
                entity.getGroupId(),
                entity.getMemberId(),
                entity.getStatus(),
                entity.getJoinedAt()
        );
    }
}
