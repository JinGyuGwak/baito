package com.baito.my_app.membership.adapter.out.persistence;

import com.baito.my_app.membership.domain.MembershipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_membership",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_membership_group_member",
                columnNames = {"group_id", "member_id"})
)
public class GroupMembershipJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    protected GroupMembershipJpaEntity() {
    }

    public GroupMembershipJpaEntity(Long id, Long groupId, Long memberId, MembershipStatus status,
                                    LocalDateTime joinedAt) {
        this.id = id;
        this.groupId = groupId;
        this.memberId = memberId;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
