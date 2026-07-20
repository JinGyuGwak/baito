package com.baito.my_app.assignment.application.service;

import com.baito.my_app.assignment.application.port.in.AssignShiftUseCase;
import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.assignment.domain.ShiftNotAvailableException;
import com.baito.my_app.assignment.domain.StaffQuotaExceededException;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import com.baito.my_app.membership.domain.NotGroupMemberException;
import com.baito.my_app.schedule.application.port.out.AvailabilitySlotRepository;
import com.baito.my_app.schedule.application.port.out.RequiredStaffSlotRepository;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit test — no Spring, no DB. Exercises requirement 7 via in-memory fake ports.
 */
class AssignShiftServiceTest {

    private static final Long GROUP_ID = 1L;
    private static final Long OWNER_ID = 10L;
    private static final Long MEMBER_ID = 20L;
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);

    private FakeWorkGroupRepository groups;
    private FakeMembershipRepository memberships;
    private FakeAvailabilityRepository availability;
    private FakeRequiredStaffRepository required;
    private FakeAssignmentRepository assignments;
    private AssignShiftService service;

    @BeforeEach
    void setUp() {
        groups = new FakeWorkGroupRepository();
        memberships = new FakeMembershipRepository();
        availability = new FakeAvailabilityRepository();
        required = new FakeRequiredStaffRepository();
        assignments = new FakeAssignmentRepository();
        service = new AssignShiftService(groups, memberships, availability, required, assignments);

        groups.save(new WorkGroup(GROUP_ID, OWNER_ID, "매장", null, null));
        memberships.save(GroupMembership.activate(GROUP_ID, MEMBER_ID, null));
    }

    private AssignShiftUseCase.Command command(LocalTime start, LocalTime end) {
        return new AssignShiftUseCase.Command(GROUP_ID, OWNER_ID, MEMBER_ID, DATE, start, end);
    }

    @Test
    void assigns_all_slots_when_available_and_within_quota() {
        // 08:00~09:00 = two slots, each needs 1, none filled, member available for both
        markAvailable(LocalTime.of(8, 0), LocalTime.of(8, 30));
        setRequired(LocalTime.of(8, 0), 1);
        setRequired(LocalTime.of(8, 30), 1);

        int assigned = service.assign(command(LocalTime.of(8, 0), LocalTime.of(9, 0)));

        assertThat(assigned).isEqualTo(2);
        assertThat(assignments.saved).hasSize(2);
    }

    @Test
    void rejects_when_slot_outside_availability() {
        // Available only for the first slot; request spans two.
        markAvailable(LocalTime.of(8, 0));
        setRequired(LocalTime.of(8, 0), 1);
        setRequired(LocalTime.of(8, 30), 1);

        assertThatThrownBy(() -> service.assign(command(LocalTime.of(8, 0), LocalTime.of(9, 0))))
                .isInstanceOf(ShiftNotAvailableException.class);
        assertThat(assignments.saved).isEmpty(); // all-or-nothing
    }

    @Test
    void rejects_when_quota_already_filled() {
        markAvailable(LocalTime.of(8, 0));
        setRequired(LocalTime.of(8, 0), 1);
        assignments.seedConfirmedCount(LocalTime.of(8, 0), 1); // already full

        assertThatThrownBy(() -> service.assign(command(LocalTime.of(8, 0), LocalTime.of(8, 30))))
                .isInstanceOf(StaffQuotaExceededException.class);
        assertThat(assignments.saved).isEmpty();
    }

    @Test
    void rejects_when_no_required_staff_configured() {
        markAvailable(LocalTime.of(8, 0)); // available but 0 headcount required
        assertThatThrownBy(() -> service.assign(command(LocalTime.of(8, 0), LocalTime.of(8, 30))))
                .isInstanceOf(StaffQuotaExceededException.class);
    }

    @Test
    void rejects_when_requester_is_not_owner() {
        AssignShiftUseCase.Command notOwner =
                new AssignShiftUseCase.Command(GROUP_ID, 999L, MEMBER_ID, DATE, LocalTime.of(8, 0), LocalTime.of(8, 30));
        assertThatThrownBy(() -> service.assign(notOwner)).isInstanceOf(NotGroupOwnerException.class);
    }

    @Test
    void rejects_when_target_is_not_active_member() {
        AssignShiftUseCase.Command otherMember =
                new AssignShiftUseCase.Command(GROUP_ID, OWNER_ID, 888L, DATE, LocalTime.of(8, 0), LocalTime.of(8, 30));
        assertThatThrownBy(() -> service.assign(otherMember)).isInstanceOf(NotGroupMemberException.class);
    }

    @Test
    void skips_slots_the_member_is_already_confirmed_for() {
        markAvailable(LocalTime.of(8, 0), LocalTime.of(8, 30));
        setRequired(LocalTime.of(8, 0), 1);
        setRequired(LocalTime.of(8, 30), 1);
        assignments.seedConfirmedMember(LocalTime.of(8, 0)); // already assigned to first slot

        int assigned = service.assign(command(LocalTime.of(8, 0), LocalTime.of(9, 0)));

        assertThat(assigned).isEqualTo(1); // only the second slot is new
    }

    private void markAvailable(LocalTime... starts) {
        for (LocalTime s : starts) {
            availability.available.add(key(GROUP_ID, MEMBER_ID, DATE, s));
        }
    }

    private void setRequired(LocalTime start, int count) {
        required.byKey.put(key(GROUP_ID, DATE, start), RequiredStaffSlot.of(GROUP_ID, DATE, start, count));
    }

    private static String key(Object... parts) {
        return String.join("|", java.util.Arrays.stream(parts).map(String::valueOf).toList());
    }

    // ---- Fakes ----

    static class FakeWorkGroupRepository implements WorkGroupRepository {
        private final Map<Long, WorkGroup> store = new HashMap<>();

        @Override
        public WorkGroup save(WorkGroup group) {
            store.put(group.id(), group);
            return group;
        }

        @Override
        public Optional<WorkGroup> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<WorkGroup> findByOwnerId(Long ownerId) {
            return store.values().stream().filter(g -> g.ownerId().equals(ownerId)).toList();
        }
    }

    static class FakeMembershipRepository implements GroupMembershipRepository {
        private final Set<String> active = new HashSet<>();

        @Override
        public GroupMembership save(GroupMembership membership) {
            if (membership.isActive()) {
                active.add(key(membership.groupId(), membership.memberId()));
            }
            return membership;
        }

        @Override
        public Optional<GroupMembership> findByGroupIdAndMemberId(Long groupId, Long memberId) {
            return Optional.empty();
        }

        @Override
        public boolean existsActiveMembership(Long groupId, Long memberId) {
            return active.contains(key(groupId, memberId));
        }

        @Override
        public List<GroupMembership> findActiveByMemberId(Long memberId) {
            return List.of();
        }
    }

    static class FakeAvailabilityRepository implements AvailabilitySlotRepository {
        private final Set<String> available = new HashSet<>();

        @Override
        public void saveAll(List<AvailabilitySlot> slots) {
        }

        @Override
        public void deleteByGroupIdAndMemberIdAndWorkDate(Long groupId, Long memberId, LocalDate workDate) {
        }

        @Override
        public List<AvailabilitySlot> findByGroupIdAndMemberIdAndWorkDate(Long groupId, Long memberId, LocalDate workDate) {
            return List.of();
        }

        @Override
        public boolean existsByGroupIdAndMemberIdAndWorkDateAndStartTime(Long groupId, Long memberId, LocalDate workDate, LocalTime startTime) {
            return available.contains(key(groupId, memberId, workDate, startTime));
        }
    }

    static class FakeRequiredStaffRepository implements RequiredStaffSlotRepository {
        private final Map<String, RequiredStaffSlot> byKey = new HashMap<>();

        @Override
        public void saveAll(List<RequiredStaffSlot> slots) {
        }

        @Override
        public void deleteByGroupIdAndWorkDate(Long groupId, LocalDate workDate) {
        }

        @Override
        public List<RequiredStaffSlot> findByGroupIdAndWorkDate(Long groupId, LocalDate workDate) {
            return List.of();
        }

        @Override
        public Optional<RequiredStaffSlot> findByGroupIdAndWorkDateAndStartTime(Long groupId, LocalDate workDate, LocalTime startTime) {
            return Optional.ofNullable(byKey.get(key(groupId, workDate, startTime)));
        }
    }

    static class FakeAssignmentRepository implements ShiftAssignmentRepository {
        private final List<ShiftAssignment> saved = new ArrayList<>();
        private final Map<String, Integer> seededCounts = new HashMap<>();
        private final Set<String> seededMembers = new HashSet<>();

        void seedConfirmedCount(LocalTime start, int count) {
            seededCounts.put(key(GROUP_ID, DATE, start), count);
        }

        void seedConfirmedMember(LocalTime start) {
            seededMembers.add(key(GROUP_ID, MEMBER_ID, DATE, start));
            seededCounts.merge(key(GROUP_ID, DATE, start), 1, Integer::sum);
        }

        @Override
        public void saveAll(List<ShiftAssignment> assignments) {
            saved.addAll(assignments);
        }

        @Override
        public int countConfirmed(Long groupId, LocalDate workDate, LocalTime startTime) {
            return seededCounts.getOrDefault(key(groupId, workDate, startTime), 0);
        }

        @Override
        public boolean existsConfirmed(Long groupId, Long memberId, LocalDate workDate, LocalTime startTime) {
            return seededMembers.contains(key(groupId, memberId, workDate, startTime));
        }

        @Override
        public List<ShiftAssignment> findConfirmedByGroupIdAndWorkDate(Long groupId, LocalDate workDate) {
            return List.copyOf(saved);
        }
    }
}
