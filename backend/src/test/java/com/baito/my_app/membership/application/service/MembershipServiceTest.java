package com.baito.my_app.membership.application.service;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.membership.application.port.in.RemoveMemberUseCase;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import com.baito.my_app.membership.domain.MembershipStatus;
import com.baito.my_app.membership.domain.NotGroupMemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    private static final Long GROUP_ID = 10L;
    private static final Long OWNER_ID = 1L;
    private static final Long MEMBER_ID = 2L;

    @Mock
    private WorkGroupRepository workGroupRepository;
    @Mock
    private GroupMembershipRepository membershipRepository;
    @InjectMocks
    private MembershipService service;

    private RemoveMemberUseCase.Command command() {
        return new RemoveMemberUseCase.Command(GROUP_ID, OWNER_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("추방 성공 - 활성 멤버십을 INACTIVE로 바꿔 저장한다")
    void remove() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null)));
        given(membershipRepository.findByGroupIdAndMemberId(GROUP_ID, MEMBER_ID))
                .willReturn(Optional.of(GroupMembership.activate(GROUP_ID, MEMBER_ID, LocalDateTime.now())));

        service.remove(command());

        ArgumentCaptor<GroupMembership> captor = ArgumentCaptor.captor();
        then(membershipRepository).should().save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MembershipStatus.INACTIVE);
    }

    @Test
    @DisplayName("추방 실패 - 소유자가 아니면 NotGroupOwnerException")
    void remove_notOwner() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, 999L, "강남점", null, null)));

        assertThatThrownBy(() -> service.remove(command()))
                .isInstanceOf(NotGroupOwnerException.class);
        then(membershipRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("추방 실패 - 활성 멤버십이 없으면 NotGroupMemberException")
    void remove_notMember() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null)));
        given(membershipRepository.findByGroupIdAndMemberId(GROUP_ID, MEMBER_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.remove(command()))
                .isInstanceOf(NotGroupMemberException.class);
    }

    @Test
    @DisplayName("추방 실패 - 이미 INACTIVE인 멤버십도 NotGroupMemberException")
    void remove_alreadyInactive() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null)));
        GroupMembership inactive = GroupMembership.activate(GROUP_ID, MEMBER_ID, LocalDateTime.now());
        inactive.deactivate();
        given(membershipRepository.findByGroupIdAndMemberId(GROUP_ID, MEMBER_ID))
                .willReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.remove(command()))
                .isInstanceOf(NotGroupMemberException.class);
    }
}
