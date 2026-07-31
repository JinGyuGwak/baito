package com.baito.my_app.schedule.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "availability_slot",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_availability_slot",
                columnNames = {"group_id", "member_id", "work_date", "start_time"})
)
public class AvailabilitySlotJpaEntity {

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

    protected AvailabilitySlotJpaEntity() {
    }

    public AvailabilitySlotJpaEntity(Long id, Long groupId, Long memberId, LocalDate workDate, LocalTime startTime) {
        this.id = id;
        this.groupId = groupId;
        this.memberId = memberId;
        this.workDate = workDate;
        this.startTime = startTime;
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
}
