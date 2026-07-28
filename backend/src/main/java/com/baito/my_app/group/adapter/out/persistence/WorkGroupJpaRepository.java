package com.baito.my_app.group.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkGroupJpaRepository extends JpaRepository<WorkGroupJpaEntity, Long> {

    List<WorkGroupJpaEntity> findByOwnerId(Long ownerId);
}
