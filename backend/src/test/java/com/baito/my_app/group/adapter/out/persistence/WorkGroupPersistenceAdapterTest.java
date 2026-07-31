package com.baito.my_app.group.adapter.out.persistence;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.support.PersistenceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(WorkGroupPersistenceAdapter.class)
class WorkGroupPersistenceAdapterTest extends PersistenceTestSupport {

    @Autowired
    private WorkGroupRepository workGroupRepository;

    @Test
    @DisplayName("저장하면 id/createdAt이 채워지고 findById로 조회된다")
    void save_and_findById() {
        WorkGroup saved = workGroupRepository.save(WorkGroup.create(1L, "강남점", "강남역 1호점"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(workGroupRepository.findById(saved.getId()))
                .get()
                .satisfies(g -> {
                    assertThat(g.getOwnerId()).isEqualTo(1L);
                    assertThat(g.getName()).isEqualTo("강남점");
                });
    }

    @Test
    @DisplayName("findAllByIds - 주어진 id들의 그룹만 조회한다")
    void findAllByIds() {
        WorkGroup gangnam = workGroupRepository.save(WorkGroup.create(1L, "강남점", null));
        WorkGroup hongdae = workGroupRepository.save(WorkGroup.create(1L, "홍대점", null));
        workGroupRepository.save(WorkGroup.create(2L, "남의점", null));

        assertThat(workGroupRepository.findAllByIds(List.of(gangnam.getId(), hongdae.getId())))
                .extracting(WorkGroup::getName)
                .containsExactlyInAnyOrder("강남점", "홍대점");
        assertThat(workGroupRepository.findAllByIds(List.of())).isEmpty();
    }

    @Test
    @DisplayName("findByOwnerId - 소유자별로만 조회한다")
    void findByOwnerId() {
        workGroupRepository.save(WorkGroup.create(1L, "강남점", null));
        workGroupRepository.save(WorkGroup.create(1L, "홍대점", null));
        workGroupRepository.save(WorkGroup.create(2L, "남의점", null));

        assertThat(workGroupRepository.findByOwnerId(1L))
                .extracting(WorkGroup::getName)
                .containsExactlyInAnyOrder("강남점", "홍대점");
        assertThat(workGroupRepository.findByOwnerId(99L)).isEmpty();
    }
}
