package com.baito.my_app.membership.application.service;

import com.baito.my_app.group.application.port.out.WorkGroupRepository;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.membership.application.port.in.GetGroupMembersQuery;
import com.baito.my_app.membership.application.port.in.GetJoinedGroupsQuery;
import com.baito.my_app.membership.application.port.out.GroupMembershipRepository;
import com.baito.my_app.membership.domain.GroupMembership;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MembershipQueryService implements GetJoinedGroupsQuery, GetGroupMembersQuery {

    private final GroupMembershipRepository membershipRepository;
    private final WorkGroupRepository workGroupRepository;
    private final MemberRepository memberRepository;

    public MembershipQueryService(GroupMembershipRepository membershipRepository,
                                  WorkGroupRepository workGroupRepository,
                                  MemberRepository memberRepository) {
        this.membershipRepository = membershipRepository;
        this.workGroupRepository = workGroupRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public List<WorkGroup> getJoinedGroups(Long memberId) {
        return membershipRepository.findActiveByMemberId(memberId).stream()
                .map(GroupMembership::getGroupId)
                .map(workGroupRepository::findById)
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    @Override
    public List<GroupMember> getGroupMembers(Long groupId, Long ownerId) {
        WorkGroup group = workGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotGroupOwnerException(groupId));
        if (!group.isOwnedBy(ownerId)) {
            throw new NotGroupOwnerException(groupId);
        }

        List<GroupMembership> memberships = membershipRepository.findActiveByGroupId(groupId);
        Map<Long, Member> membersById = memberRepository.findAllByIds(
                        memberships.stream().map(GroupMembership::getMemberId).toList()).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        return memberships.stream()
                .map(ms -> {
                    Member member = membersById.get(ms.getMemberId());
                    return new GroupMember(
                            ms.getMemberId(),
                            member != null ? member.getName() : null,
                            member != null ? member.getLoginId() : null,
                            ms.getJoinedAt());
                })
                .sorted(Comparator.comparing(GroupMember::name,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}
