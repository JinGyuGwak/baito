package com.baito.my_app.assignment.application.service;

import com.baito.my_app.assignment.application.port.in.AssignShiftUseCase;
import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.assignment.domain.ShiftNotAvailableException;
import com.baito.my_app.assignment.domain.StaffQuotaExceededException;
import com.baito.my_app.common.domain.SlotTimes;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.NotGroupMemberException;
import com.baito.my_app.schedule.application.port.out.AvailabilitySlotRepository;
import com.baito.my_app.schedule.application.port.out.RequiredStaffSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure domain service (depends only on outbound ports — no JPA/MySQL types). Implements the
 * assignment validation of requirement 7, slot by slot; all slots must pass before anything is saved.
 */
@Service
@Transactional
public class AssignShiftService implements AssignShiftUseCase {

    private final WorkGroupRepository workGroupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final RequiredStaffSlotRepository requiredStaffSlotRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    public AssignShiftService(WorkGroupRepository workGroupRepository,
                              GroupMembershipRepository membershipRepository,
                              AvailabilitySlotRepository availabilitySlotRepository,
                              RequiredStaffSlotRepository requiredStaffSlotRepository,
                              ShiftAssignmentRepository shiftAssignmentRepository) {
        this.workGroupRepository = workGroupRepository;
        this.membershipRepository = membershipRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.requiredStaffSlotRepository = requiredStaffSlotRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

    @Override
    public int assign(Command command) {
        // 1. Requester must be the group owner.
        WorkGroup group = workGroupRepository.findById(command.getGroupId())
                .orElseThrow(() -> new NotGroupOwnerException(command.getGroupId()));
        if (!group.isOwnedBy(command.getAssignerId())) {
            throw new NotGroupOwnerException(command.getGroupId());
        }

        // 2. Target must be an active member of the group.
        if (!membershipRepository.existsActiveMembership(command.getGroupId(), command.getMemberId())) {
            throw new NotGroupMemberException(command.getGroupId());
        }

        // 3. Decompose the requested range into 30-minute slots.
        List<LocalTime> slots = SlotTimes.expand(command.getStartTime(), command.getEndTime());

        LocalDate date = command.getWorkDate();
        List<ShiftAssignment> toCreate = new ArrayList<>();
        for (LocalTime slot : slots) {
            // Idempotent: a slot the member is already confirmed for needs no re-check or re-insert.
            if (shiftAssignmentRepository.existsConfirmed(command.getGroupId(), command.getMemberId(), date, slot)) {
                continue;
            }
            // 4. Availability check.
            boolean available = availabilitySlotRepository
                    .existsByGroupIdAndMemberIdAndWorkDateAndStartTime(command.getGroupId(), command.getMemberId(), date, slot);
            if (!available) {
                throw new ShiftNotAvailableException(date, slot);
            }
            // 5. Quota check (required headcount vs. currently confirmed). No configured slot => 0 required.
            int required = requiredStaffSlotRepository
                    .findByGroupIdAndWorkDateAndStartTime(command.getGroupId(), date, slot)
                    .map(s -> s.getRequiredCount())
                    .orElse(0);
            int confirmed = shiftAssignmentRepository.countConfirmed(command.getGroupId(), date, slot);
            if (confirmed >= required) {
                throw new StaffQuotaExceededException(date, slot, required);
            }
            toCreate.add(ShiftAssignment.confirm(command.getGroupId(), command.getMemberId(), date, slot, command.getAssignerId()));
        }

        // 6. All slots passed — persist. (Note: the count read in step 5 is not row-locked, so under
        //    heavy concurrent assignment a DB-level guard/lock would be needed to fully prevent races.)
        shiftAssignmentRepository.saveAll(toCreate);
        return toCreate.size();
    }
}
