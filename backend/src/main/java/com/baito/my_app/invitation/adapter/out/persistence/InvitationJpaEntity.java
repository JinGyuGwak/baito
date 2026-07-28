package com.baito.my_app.invitation.adapter.out.persistence;

import com.baito.my_app.invitation.domain.InvitationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "invitation",
        indexes = {
                @Index(name = "idx_invitation_inviter", columnList = "inviter_id"),
                @Index(name = "idx_invitation_invitee_status", columnList = "invitee_id, status"),
                @Index(name = "idx_invitation_group_invitee_status", columnList = "group_id, invitee_id, status")
        }
)
public class InvitationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "inviter_id", nullable = false)
    private Long inviterId;

    @Column(name = "invitee_id", nullable = false)
    private Long inviteeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    protected InvitationJpaEntity() {
    }

    public InvitationJpaEntity(Long id, Long groupId, Long inviterId, Long inviteeId, InvitationStatus status,
                               LocalDateTime createdAt, LocalDateTime respondedAt) {
        this.id = id;
        this.groupId = groupId;
        this.inviterId = inviterId;
        this.inviteeId = inviteeId;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getInviterId() {
        return inviterId;
    }

    public Long getInviteeId() {
        return inviteeId;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }
}
