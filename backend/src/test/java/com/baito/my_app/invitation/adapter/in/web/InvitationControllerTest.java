package com.baito.my_app.invitation.adapter.in.web;

import com.baito.my_app.invitation.application.port.in.CancelInvitationUseCase;
import com.baito.my_app.invitation.application.port.in.GetInvitationsQuery;
import com.baito.my_app.invitation.application.port.in.InviteMemberUseCase;
import com.baito.my_app.invitation.application.port.in.RespondInvitationUseCase;
import com.baito.my_app.invitation.domain.Invitation;
import com.baito.my_app.invitation.domain.InvitationStatus;
import com.baito.my_app.support.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationController.class)
class InvitationControllerTest extends RestDocsSupport {

    @MockitoBean
    private InviteMemberUseCase inviteMemberUseCase;
    @MockitoBean
    private CancelInvitationUseCase cancelInvitationUseCase;
    @MockitoBean
    private RespondInvitationUseCase respondInvitationUseCase;
    @MockitoBean
    private GetInvitationsQuery getInvitationsQuery;

    @Test
    @DisplayName("초대 생성 - OWNER가 알바를 로그인 ID로 초대, 201 Created")
    void invite() throws Exception {
        given(inviteMemberUseCase.invite(any())).willReturn(50L);

        String body = objectMapper.writeValueAsString(Map.of(
                "groupId", 10,
                "inviteeLoginId", "worker01"));

        mockMvc.perform(post("/api/invitations").with(owner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invitationId").value(50))
                .andDo(document("invitation-invite",
                        requestFields(
                                fieldWithPath("groupId").description("초대할 그룹 ID"),
                                fieldWithPath("inviteeLoginId").description("초대받는 알바의 로그인 ID")),
                        responseFields(
                                fieldWithPath("invitationId").description("생성된 초대 ID"))));
    }

    @Test
    @DisplayName("보낸 초대 목록 조회 - OWNER")
    void sent() throws Exception {
        given(getInvitationsQuery.getSentInvitations(1L)).willReturn(List.of(
                new Invitation(50L, 10L, 1L, 2L, InvitationStatus.PENDING,
                        LocalDateTime.of(2026, 5, 1, 9, 0), null)));

        mockMvc.perform(get("/api/invitations/sent").with(owner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andDo(document("invitation-sent",
                        responseFields(
                                fieldWithPath("[].id").description("초대 ID"),
                                fieldWithPath("[].groupId").description("그룹 ID"),
                                fieldWithPath("[].inviterId").description("초대한 사장 회원 ID"),
                                fieldWithPath("[].inviteeId").description("초대받은 알바 회원 ID"),
                                fieldWithPath("[].status").description("상태: `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`"),
                                fieldWithPath("[].createdAt").description("생성 일시"),
                                fieldWithPath("[].respondedAt").optional().description("응답 일시 (미응답 시 null)"))));
    }

    @Test
    @DisplayName("초대 취소 - OWNER, 204 No Content")
    void cancel() throws Exception {
        mockMvc.perform(post("/api/invitations/{invitationId}/cancel", 50L).with(owner()))
                .andExpect(status().isNoContent())
                .andDo(document("invitation-cancel",
                        pathParameters(parameterWithName("invitationId").description("취소할 초대 ID"))));
    }

    @Test
    @DisplayName("받은 초대 목록 조회 - PART_TIMER (PENDING 상태만)")
    void received() throws Exception {
        given(getInvitationsQuery.getReceivedPendingInvitations(2L)).willReturn(List.of(
                new Invitation(50L, 10L, 1L, 2L, InvitationStatus.PENDING,
                        LocalDateTime.of(2026, 5, 1, 9, 0), null)));

        mockMvc.perform(get("/api/invitations/received").with(partTimer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andDo(document("invitation-received",
                        responseFields(
                                fieldWithPath("[].id").description("초대 ID"),
                                fieldWithPath("[].groupId").description("그룹 ID"),
                                fieldWithPath("[].inviterId").description("초대한 사장 회원 ID"),
                                fieldWithPath("[].inviteeId").description("초대받은 알바 회원 ID"),
                                fieldWithPath("[].status").description("상태 (항상 `PENDING`)"),
                                fieldWithPath("[].createdAt").description("생성 일시"),
                                fieldWithPath("[].respondedAt").optional().description("응답 일시 (미응답 시 null)"))));
    }

    @Test
    @DisplayName("초대 수락 - PART_TIMER, 204 No Content")
    void accept() throws Exception {
        mockMvc.perform(post("/api/invitations/{invitationId}/accept", 50L).with(partTimer()))
                .andExpect(status().isNoContent())
                .andDo(document("invitation-accept",
                        pathParameters(parameterWithName("invitationId").description("수락할 초대 ID"))));
    }

    @Test
    @DisplayName("초대 거절 - PART_TIMER, 204 No Content")
    void reject() throws Exception {
        mockMvc.perform(post("/api/invitations/{invitationId}/reject", 50L).with(partTimer()))
                .andExpect(status().isNoContent())
                .andDo(document("invitation-reject",
                        pathParameters(parameterWithName("invitationId").description("거절할 초대 ID"))));
    }
}
