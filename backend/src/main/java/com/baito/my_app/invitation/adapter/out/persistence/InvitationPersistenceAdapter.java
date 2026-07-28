package com.baito.my_app.invitation.adapter.out.persistence;

import com.baito.my_app.common.domain.PageResult;
import com.baito.my_app.invitation.application.port.out.InvitationRepository;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InvitationPersistenceAdapter implements InvitationRepository {

    private final InvitationJpaRepository jpaRepository;

    public InvitationPersistenceAdapter(InvitationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Invitation save(Invitation invitation) {
        return toDomain(jpaRepository.save(toEntity(invitation)));
    }

    @Override
    public Optional<Invitation> findById(Long id) {
        return jpaRepository.findById(id).map(InvitationPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsPendingByGroupIdAndInviteeId(Long groupId, Long inviteeId) {
        return jpaRepository.existsByGroupIdAndInviteeIdAndStatus(groupId, inviteeId, InvitationStatus.PENDING);
    }

    @Override
    public PageResult<Invitation> findByInviterIdAndGroupId(Long inviterId, Long groupId, InvitationStatus status,
                                                            int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<InvitationJpaEntity> result = (status == null)
                ? jpaRepository.findByInviterIdAndGroupId(inviterId, groupId, pageable)
                : jpaRepository.findByInviterIdAndGroupIdAndStatus(inviterId, groupId, status, pageable);
        return new PageResult<>(
                result.getContent().stream().map(InvitationPersistenceAdapter::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public List<Invitation> findPendingByInviteeId(Long inviteeId) {
        return jpaRepository.findByInviteeIdAndStatusOrderByCreatedAtDesc(inviteeId, InvitationStatus.PENDING).stream()
                .map(InvitationPersistenceAdapter::toDomain)
                .toList();
    }

    private static InvitationJpaEntity toEntity(Invitation invitation) {
        return new InvitationJpaEntity(
                invitation.getId(),
                invitation.getGroupId(),
                invitation.getInviterId(),
                invitation.getInviteeId(),
                invitation.getStatus(),
                invitation.getCreatedAt(),
                invitation.getRespondedAt()
        );
    }

    private static Invitation toDomain(InvitationJpaEntity entity) {
        return new Invitation(
                entity.getId(),
                entity.getGroupId(),
                entity.getInviterId(),
                entity.getInviteeId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getRespondedAt()
        );
    }
}
