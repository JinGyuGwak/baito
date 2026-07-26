package com.baito.my_app.invitation.adapter.in.web;

import com.baito.my_app.common.security.LoginMember;
import com.baito.my_app.invitation.application.port.in.CancelInvitationUseCase;
import com.baito.my_app.invitation.application.port.in.GetInvitationsQuery;
import com.baito.my_app.invitation.application.port.in.InviteMemberUseCase;
import com.baito.my_app.invitation.application.port.in.RespondInvitationUseCase;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InviteMemberUseCase inviteMemberUseCase;
    private final CancelInvitationUseCase cancelInvitationUseCase;
    private final RespondInvitationUseCase respondInvitationUseCase;
    private final GetInvitationsQuery getInvitationsQuery;

    public InvitationController(InviteMemberUseCase inviteMemberUseCase,
                                CancelInvitationUseCase cancelInvitationUseCase,
                                RespondInvitationUseCase respondInvitationUseCase,
                                GetInvitationsQuery getInvitationsQuery) {
        this.inviteMemberUseCase = inviteMemberUseCase;
        this.cancelInvitationUseCase = cancelInvitationUseCase;
        this.respondInvitationUseCase = respondInvitationUseCase;
        this.getInvitationsQuery = getInvitationsQuery;
    }

    // ---- Owner side ----

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<InviteResponse> invite(@Valid @RequestBody InviteRequest request,
                                                 @AuthenticationPrincipal LoginMember loginMember) {
        Long id = inviteMemberUseCase.invite(new InviteMemberUseCase.Command(
                request.getGroupId(), loginMember.getMemberId(), request.getInviteeLoginId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new InviteResponse(id));
    }

    @GetMapping("/sent")
    @PreAuthorize("hasRole('OWNER')")
    public List<InvitationResponse> sent(@AuthenticationPrincipal LoginMember loginMember) {
        return getInvitationsQuery.getSentInvitations(loginMember.getMemberId()).stream()
                .map(InvitationResponse::from)
                .toList();
    }

    @PostMapping("/{invitationId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OWNER')")
    public void cancel(@PathVariable Long invitationId, @AuthenticationPrincipal LoginMember loginMember) {
        cancelInvitationUseCase.cancel(
                new CancelInvitationUseCase.Command(invitationId, loginMember.getMemberId()));
    }

    // ---- Part-timer side ----

    @GetMapping("/received")
    @PreAuthorize("hasRole('PART_TIMER')")
    public List<InvitationResponse> received(@AuthenticationPrincipal LoginMember loginMember) {
        return getInvitationsQuery.getReceivedPendingInvitations(loginMember.getMemberId()).stream()
                .map(InvitationResponse::from)
                .toList();
    }

    @PostMapping("/{invitationId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PART_TIMER')")
    public void accept(@PathVariable Long invitationId, @AuthenticationPrincipal LoginMember loginMember) {
        respondInvitationUseCase.accept(
                new RespondInvitationUseCase.Command(invitationId, loginMember.getMemberId()));
    }

    @PostMapping("/{invitationId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PART_TIMER')")
    public void reject(@PathVariable Long invitationId, @AuthenticationPrincipal LoginMember loginMember) {
        respondInvitationUseCase.reject(
                new RespondInvitationUseCase.Command(invitationId, loginMember.getMemberId()));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class InviteRequest {
        @NotNull
        private Long groupId;
        @NotBlank
        private String inviteeLoginId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class InviteResponse {
        private Long invitationId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class InvitationResponse {
        private Long id;
        private Long groupId;
        private Long inviterId;
        private Long inviteeId;
        private InvitationStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime respondedAt;

        static InvitationResponse from(Invitation i) {
            return new InvitationResponse(i.getId(), i.getGroupId(), i.getInviterId(), i.getInviteeId(),
                    i.getStatus(), i.getCreatedAt(), i.getRespondedAt());
        }
    }
}
