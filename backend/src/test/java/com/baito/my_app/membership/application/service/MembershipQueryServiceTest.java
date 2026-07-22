package com.baito.my_app.membership.application.service;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MembershipQueryServiceTest {

    private static final Long MEMBER_ID = 2L;

    @Mock
    private GroupMembershipRepository membershipRepository;
    @Mock
    private WorkGroupRepository workGroupRepository;
    @InjectMocks
    private MembershipQueryService service;

    @Test
    @DisplayName("활성 멤버십의 그룹들을 조회해 반환한다")
    void getJoinedGroups() {
        given(membershipRepository.findActiveByMemberId(MEMBER_ID)).willReturn(List.of(
                GroupMembership.activate(10L, MEMBER_ID, LocalDateTime.now()),
                GroupMembership.activate(20L, MEMBER_ID, LocalDateTime.now())));
        given(workGroupRepository.findById(10L))
                .willReturn(Optional.of(new WorkGroup(10L, 1L, "강남점", null, null)));
        given(workGroupRepository.findById(20L))
                .willReturn(Optional.of(new WorkGroup(20L, 1L, "홍대점", null, null)));

        assertThat(service.getJoinedGroups(MEMBER_ID))
                .extracting(WorkGroup::getId)
                .containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("멤버십은 있으나 그룹이 조회되지 않으면 결과에서 제외한다")
    void getJoinedGroups_skipsMissingGroup() {
        given(membershipRepository.findActiveByMemberId(MEMBER_ID)).willReturn(List.of(
                GroupMembership.activate(10L, MEMBER_ID, LocalDateTime.now()),
                GroupMembership.activate(20L, MEMBER_ID, LocalDateTime.now())));
        given(workGroupRepository.findById(10L))
                .willReturn(Optional.of(new WorkGroup(10L, 1L, "강남점", null, null)));
        given(workGroupRepository.findById(20L)).willReturn(Optional.empty());

        assertThat(service.getJoinedGroups(MEMBER_ID))
                .extracting(WorkGroup::getId)
                .containsExactly(10L);
    }
}
