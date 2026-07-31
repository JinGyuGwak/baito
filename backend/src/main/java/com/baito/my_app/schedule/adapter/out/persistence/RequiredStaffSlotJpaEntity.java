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
        name = "required_staff_slot",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_required_staff_slot",
                columnNames = {"group_id", "work_date", "start_time"})
)
public class RequiredStaffSlotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "required_count", nullable = false)
    private int requiredCount;

    protected RequiredStaffSlotJpaEntity() {
    }

    public RequiredStaffSlotJpaEntity(Long id, Long groupId, LocalDate workDate, LocalTime startTime, int requiredCount) {
        this.id = id;
        this.groupId = groupId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.requiredCount = requiredCount;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getRequiredCount() {
        return requiredCount;
    }
}
