package com.baito.my_app.group.application.service;

import com.baito.my_app.group.application.port.in.CreateGroupUseCase;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.WorkGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private WorkGroupRepository workGroupRepository;
    @InjectMocks
    private GroupService service;

    @Test
    @DisplayName("그룹 생성 - 소유자를 담은 그룹을 저장하고 생성된 id를 반환한다")
    void createGroup() {
        given(workGroupRepository.save(any())).willReturn(new WorkGroup(10L, 1L, "강남점", "설명", null));

        Long id = service.createGroup(new CreateGroupUseCase.Command(1L, "강남점", "설명"));

        assertThat(id).isEqualTo(10L);
        ArgumentCaptor<WorkGroup> captor = ArgumentCaptor.forClass(WorkGroup.class);
        verify(workGroupRepository).save(captor.capture());
        assertThat(captor.getValue().getOwnerId()).isEqualTo(1L);
        assertThat(captor.getValue().getId()).isNull(); // 저장 전이므로 id 미할당
    }

    @Test
    @DisplayName("소유 그룹 목록 - 소유자 id로 조회 결과를 그대로 반환한다")
    void getOwnedGroups() {
        List<WorkGroup> groups = List.of(new WorkGroup(10L, 1L, "강남점", null, null));
        given(workGroupRepository.findByOwnerId(1L)).willReturn(groups);

        assertThat(service.getOwnedGroups(1L)).isEqualTo(groups);
    }
}
