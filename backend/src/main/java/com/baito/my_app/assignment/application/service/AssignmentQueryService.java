package com.baito.my_app.assignment.application.service;

import com.baito.my_app.assignment.application.port.in.GetAssignmentsQuery;
import com.baito.my_app.assignment.application.port.out.ShiftAssignmentRepository;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AssignmentQueryService implements GetAssignmentsQuery {

    private final WorkGroupRepository workGroupRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final MemberRepository memberRepository;

    public AssignmentQueryService(WorkGroupRepository workGroupRepository,
                                  ShiftAssignmentRepository shiftAssignmentRepository,
                                  MemberRepository memberRepository) {
        this.workGroupRepository = workGroupRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public List<AssignmentDetail> getAssignments(Long groupId, Long ownerId, LocalDate workDate) {
        WorkGroup group = workGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotGroupOwnerException(groupId));
        if (!group.isOwnedBy(ownerId)) {
            throw new NotGroupOwnerException(groupId);
        }
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findConfirmedByGroupIdAndWorkDate(groupId, workDate);

        Map<Long, Member> membersById = memberRepository.findAllByIds(
                        assignments.stream().map(ShiftAssignment::getMemberId).distinct().toList()).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        return assignments.stream()
                .map(a -> {
                    Member member = membersById.get(a.getMemberId());
                    return new AssignmentDetail(
                            a.getMemberId(),
                            member != null ? member.getName() : null,
                            member != null ? member.getLoginId() : null,
                            a.getStartTime());
                })
                .toList();
    }

    @Override
    public List<ShiftAssignment> getMyAssignments(Long memberId, LocalDate workDate) {
        // A member reads their own assignments — no ownership check required.
        return shiftAssignmentRepository.findConfirmedByMemberIdAndWorkDate(memberId, workDate);
    }
}
