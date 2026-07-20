package com.baito.my_app.membership.adapter.in.web;

import com.baito.my_app.common.security.LoginMember;
import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.membership.application.port.in.GetJoinedGroupsQuery;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/memberships")
@PreAuthorize("hasRole('PART_TIMER')")
public class MembershipController {

    private final GetJoinedGroupsQuery getJoinedGroupsQuery;

    public MembershipController(GetJoinedGroupsQuery getJoinedGroupsQuery) {
        this.getJoinedGroupsQuery = getJoinedGroupsQuery;
    }

    @GetMapping("/groups")
    public List<JoinedGroupResponse> myGroups(@AuthenticationPrincipal LoginMember loginMember) {
        return getJoinedGroupsQuery.getJoinedGroups(loginMember.getMemberId()).stream()
                .map(JoinedGroupResponse::from)
                .toList();
    }

    public record JoinedGroupResponse(Long id, String name, String description) {
        static JoinedGroupResponse from(WorkGroup g) {
            return new JoinedGroupResponse(g.id(), g.name(), g.description());
        }
    }
}
