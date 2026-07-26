package com.baito.my_app.assignment.application.service;

import com.baito.my_app.assignment.application.port.in.GetAssignmentsQuery;
import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AssignmentQueryService implements GetAssignmentsQuery {

    private final WorkGroupRepository workGroupRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    public AssignmentQueryService(WorkGroupRepository workGroupRepository,
                                  ShiftAssignmentRepository shiftAssignmentRepository) {
        this.workGroupRepository = workGroupRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

    @Override
    public List<ShiftAssignment> getAssignments(Long groupId, Long ownerId, LocalDate workDate) {
        WorkGroup group = workGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotGroupOwnerException(groupId));
        if (!group.isOwnedBy(ownerId)) {
            throw new NotGroupOwnerException(groupId);
        }
        return shiftAssignmentRepository.findConfirmedByGroupIdAndWorkDate(groupId, workDate);
    }

    @Override
    public List<ShiftAssignment> getMyAssignments(Long memberId, LocalDate workDate) {
        // A member reads their own assignments — no ownership check required.
        return shiftAssignmentRepository.findConfirmedByMemberIdAndWorkDate(memberId, workDate);
    }
}
