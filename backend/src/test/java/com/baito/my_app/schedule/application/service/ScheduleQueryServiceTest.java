package com.baito.my_app.schedule.application.service;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.NotGroupMemberException;
import com.baito.my_app.schedule.application.port.out.AvailabilitySlotRepository;
import com.baito.my_app.schedule.application.port.out.RequiredStaffSlotRepository;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ScheduleQueryServiceTest {

    private static final Long GROUP_ID = 10L;
    private static final Long OWNER_ID = 1L;
    private static final Long MEMBER_ID = 2L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    @Mock
    private WorkGroupRepository workGroupRepository;
    @Mock
    private GroupMembershipRepository membershipRepository;
    @Mock
    private RequiredStaffSlotRepository requiredStaffSlotRepository;
    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;
    @InjectMocks
    private ScheduleQueryService service;

    @Test
    @DisplayName("필요 인원 조회 성공 - 소유자면 슬롯 목록을 반환한다")
    void getRequiredStaff() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, OWNER_ID, "강남점", null, null)));
        List<RequiredStaffSlot> slots = List.of(RequiredStaffSlot.of(GROUP_ID, DATE, LocalTime.of(9, 0), 2));
        given(requiredStaffSlotRepository.findByGroupIdAndWorkDate(GROUP_ID, DATE)).willReturn(slots);

        assertThat(service.getRequiredStaff(GROUP_ID, OWNER_ID, DATE)).isEqualTo(slots);
    }

    @Test
    @DisplayName("필요 인원 조회 실패 - 소유자가 아니면 NotGroupOwnerException")
    void getRequiredStaff_notOwner() {
        given(workGroupRepository.findById(GROUP_ID))
                .willReturn(Optional.of(new WorkGroup(GROUP_ID, 999L, "강남점", null, null)));

        assertThatThrownBy(() -> service.getRequiredStaff(GROUP_ID, OWNER_ID, DATE))
                .isInstanceOf(NotGroupOwnerException.class);
    }

    @Test
    @DisplayName("내 근무 가능 시간 조회 성공 - 활성 멤버면 슬롯 목록을 반환한다")
    void getMyAvailability() {
        given(membershipRepository.existsActiveMembership(GROUP_ID, MEMBER_ID)).willReturn(true);
        List<AvailabilitySlot> slots = List.of(AvailabilitySlot.of(GROUP_ID, MEMBER_ID, DATE, LocalTime.of(9, 0)));
        given(availabilitySlotRepository.findByGroupIdAndMemberIdAndWorkDate(GROUP_ID, MEMBER_ID, DATE)).willReturn(slots);

        assertThat(service.getMyAvailability(GROUP_ID, MEMBER_ID, DATE)).isEqualTo(slots);
    }

    @Test
    @DisplayName("내 근무 가능 시간 조회 실패 - 활성 멤버가 아니면 NotGroupMemberException")
    void getMyAvailability_notMember() {
        given(membershipRepository.existsActiveMembership(GROUP_ID, MEMBER_ID)).willReturn(false);

        assertThatThrownBy(() -> service.getMyAvailability(GROUP_ID, MEMBER_ID, DATE))
                .isInstanceOf(NotGroupMemberException.class);
    }
}
