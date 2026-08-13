package com.baito.my_app.membership.adapter.in.web;

import com.baito.my_app.common.security.LoginMember;
import com.baito.my_app.membership.application.port.in.GetGroupMembersQuery;
import com.baito.my_app.membership.application.port.in.RemoveMemberUseCase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** Owner-side roster management: list the group's active part-timers and kick members. */
@RestController
@RequestMapping("/api/groups/{groupId}/members")
@PreAuthorize("hasRole('OWNER')")
public class GroupMemberController {

    private final GetGroupMembersQuery getGroupMembersQuery;
    private final RemoveMemberUseCase removeMemberUseCase;

    public GroupMemberController(GetGroupMembersQuery getGroupMembersQuery,
                                 RemoveMemberUseCase removeMemberUseCase) {
        this.getGroupMembersQuery = getGroupMembersQuery;
        this.removeMemberUseCase = removeMemberUseCase;
    }

    @GetMapping
    public List<GroupMemberResponse> members(@PathVariable Long groupId,
                                             @AuthenticationPrincipal LoginMember loginMember) {
        return getGroupMembersQuery.getGroupMembers(groupId, loginMember.getMemberId()).stream()
                .map(GroupMemberResponse::from)
                .toList();
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long groupId,
                       @PathVariable Long memberId,
                       @AuthenticationPrincipal LoginMember loginMember) {
        removeMemberUseCase.remove(
                new RemoveMemberUseCase.Command(groupId, loginMember.getMemberId(), memberId));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class GroupMemberResponse {
        private Long memberId;
        private String name;
        private String loginId;
        private LocalDateTime joinedAt;

        static GroupMemberResponse from(GetGroupMembersQuery.GroupMember m) {
            return new GroupMemberResponse(m.getMemberId(), m.getName(), m.getLoginId(), m.getJoinedAt());
        }
    }
}
