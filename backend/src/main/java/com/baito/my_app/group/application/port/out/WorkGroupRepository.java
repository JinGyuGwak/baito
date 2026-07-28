package com.baito.my_app.group.application.port.out;

import com.baito.my_app.group.domain.WorkGroup;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for group persistence.
 */
public interface WorkGroupRepository {

    WorkGroup save(WorkGroup group);

    Optional<WorkGroup> findById(Long id);

    List<WorkGroup> findByOwnerId(Long ownerId);
}
