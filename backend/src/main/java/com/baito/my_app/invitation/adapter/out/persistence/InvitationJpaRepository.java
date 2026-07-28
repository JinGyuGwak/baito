package com.baito.my_app.invitation.adapter.out.persistence;

import com.baito.my_app.invitation.domain.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitationJpaRepository extends JpaRepository<InvitationJpaEntity, Long> {

    boolean existsByGroupIdAndInviteeIdAndStatus(Long groupId, Long inviteeId, InvitationStatus status);

    Page<InvitationJpaEntity> findByInviterIdAndGroupId(Long inviterId, Long groupId, Pageable pageable);

    Page<InvitationJpaEntity> findByInviterIdAndGroupIdAndStatus(Long inviterId, Long groupId,
                                                                 InvitationStatus status, Pageable pageable);

    List<InvitationJpaEntity> findByInviteeIdAndStatusOrderByCreatedAtDesc(Long inviteeId, InvitationStatus status);
}
