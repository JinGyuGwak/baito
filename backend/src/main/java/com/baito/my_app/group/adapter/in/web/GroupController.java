package com.baito.my_app.group.adapter.in.web;

import com.baito.my_app.common.security.LoginMember;
import com.baito.my_app.group.application.port.in.CreateGroupUseCase;
import com.baito.my_app.group.application.port.in.GetOwnedGroupsQuery;
import com.baito.my_app.group.domain.WorkGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@PreAuthorize("hasRole('OWNER')")
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final GetOwnedGroupsQuery getOwnedGroupsQuery;

    public GroupController(CreateGroupUseCase createGroupUseCase, GetOwnedGroupsQuery getOwnedGroupsQuery) {
        this.createGroupUseCase = createGroupUseCase;
        this.getOwnedGroupsQuery = getOwnedGroupsQuery;
    }

    @PostMapping
    public ResponseEntity<CreateGroupResponse> create(@Valid @RequestBody CreateGroupRequest request,
                                                      @AuthenticationPrincipal LoginMember loginMember) {
        Long groupId = createGroupUseCase.createGroup(
                new CreateGroupUseCase.Command(loginMember.getMemberId(), request.getName(), request.getDescription()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateGroupResponse(groupId));
    }

    @GetMapping
    public List<GroupResponse> myGroups(@AuthenticationPrincipal LoginMember loginMember) {
        return getOwnedGroupsQuery.getOwnedGroups(loginMember.getMemberId()).stream()
                .map(GroupResponse::from)
                .toList();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CreateGroupRequest {
        @NotBlank
        @Size(max = 100)
        private String name;
        @Size(max = 255)
        private String description;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CreateGroupResponse {
        private Long groupId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class GroupResponse {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime createdAt;

        static GroupResponse from(WorkGroup g) {
            return new GroupResponse(g.getId(), g.getName(), g.getDescription(), g.getCreatedAt());
        }
    }
}
