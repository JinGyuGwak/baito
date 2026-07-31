package com.baito.my_app.group.adapter.out.persistence;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.WorkGroup;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class WorkGroupPersistenceAdapter implements WorkGroupRepository {

    private final WorkGroupJpaRepository jpaRepository;

    public WorkGroupPersistenceAdapter(WorkGroupJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WorkGroup save(WorkGroup group) {
        return toDomain(jpaRepository.save(toEntity(group)));
    }

    @Override
    public Optional<WorkGroup> findById(Long id) {
        return jpaRepository.findById(id).map(WorkGroupPersistenceAdapter::toDomain);
    }

    @Override
    public List<WorkGroup> findAllByIds(Collection<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(WorkGroupPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<WorkGroup> findByOwnerId(Long ownerId) {
        return jpaRepository.findByOwnerId(ownerId).stream()
                .map(WorkGroupPersistenceAdapter::toDomain)
                .toList();
    }

    private static WorkGroupJpaEntity toEntity(WorkGroup group) {
        return new WorkGroupJpaEntity(
                group.getId(),
                group.getOwnerId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedAt()
        );
    }

    private static WorkGroup toDomain(WorkGroupJpaEntity entity) {
        return new WorkGroup(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }
}
