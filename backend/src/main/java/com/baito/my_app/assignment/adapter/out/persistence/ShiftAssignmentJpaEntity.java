package com.baito.my_app.assignment.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "shift_assignment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_shift_assignment",
                columnNames = {"group_id", "member_id", "work_date", "start_time"}),
        indexes = @Index(name = "idx_shift_assignment_slot", columnList = "group_id, work_date, start_time")
)
public class ShiftAssignmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ShiftAssignmentJpaEntity() {
    }

    public ShiftAssignmentJpaEntity(Long id, Long groupId, Long memberId, LocalDate workDate, LocalTime startTime,
                                    Long assignedBy, LocalDateTime createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.memberId = memberId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.assignedBy = assignedBy;
        this.createdAt = createdAt;
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

    public LocalDate getWorkDate() {
        return workDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
