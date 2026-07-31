package com.baito.my_app.assignment.application.service;

import com.baito.my_app.assignment.application.port.in.CancelShiftUseCase;
import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.common.domain.SlotTimes;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class CancelShiftService implements CancelShiftUseCase {

    private final WorkGroupRepository workGroupRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    public CancelShiftService(WorkGroupRepository workGroupRepository,
                              ShiftAssignmentRepository shiftAssignmentRepository) {
        this.workGroupRepository = workGroupRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

    @Override
    public int cancel(Command command) {
        WorkGroup group = workGroupRepository.findById(command.getGroupId())
                .orElseThrow(() -> new NotGroupOwnerException(command.getGroupId()));
        if (!group.isOwnedBy(command.getOwnerId())) {
            throw new NotGroupOwnerException(command.getGroupId());
        }

        List<LocalTime> slots = SlotTimes.expand(command.getStartTime(), command.getEndTime());
        return shiftAssignmentRepository.deleteConfirmedInSlots(
                command.getGroupId(), command.getMemberId(), command.getWorkDate(), slots);
    }
}
