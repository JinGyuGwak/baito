package com.baito.my_app.invitation.adapter.in.web;

import com.baito.my_app.common.domain.PageResult;
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
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
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
                                fieldWithPath("groupId").description("招待するグループID"),
                                fieldWithPath("inviteeLoginId").description("招待されるアルバイトのログインID")),
                        responseFields(
                                fieldWithPath("invitationId").description("作成された招待ID"))));
    }

    @Test
    @DisplayName("보낸 초대 목록 조회 - OWNER, 그룹별 + 상태 필터 + 페이징, 초대받은 알바 이름 포함")
    void sent() throws Exception {
        given(getInvitationsQuery.getSentInvitations(1L, 10L, InvitationStatus.PENDING, 0, 10))
                .willReturn(new PageResult<>(List.of(
                        new GetInvitationsQuery.SentInvitation(
                                new Invitation(50L, 10L, 1L, 2L, InvitationStatus.PENDING,
                                        LocalDateTime.of(2026, 5, 1, 9, 0), null),
                                "김알바", "worker01")),
                        0, 10, 1, 1));

        mockMvc.perform(get("/api/invitations/sent").with(owner())
                        .param("groupId", "10")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(50))
                .andExpect(jsonPath("$.content[0].inviteeName").value("김알바"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andDo(document("invitation-sent",
                        queryParameters(
                                parameterWithName("groupId").description("照会するグループID"),
                                parameterWithName("status").optional()
                                        .description("状態フィルター: `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED` (省略時は全件)"),
                                parameterWithName("page").optional().description("ページ番号 (0から, デフォルト0)"),
                                parameterWithName("size").optional().description("ページサイズ (デフォルト10)")),
                        responseFields(
                                fieldWithPath("content[].id").description("招待ID"),
                                fieldWithPath("content[].groupId").description("グループID"),
                                fieldWithPath("content[].inviteeId").description("招待されたアルバイト会員ID"),
                                fieldWithPath("content[].inviteeName").description("招待されたアルバイトの名前"),
                                fieldWithPath("content[].inviteeLoginId").description("招待されたアルバイトのログインID"),
                                fieldWithPath("content[].status").description("状態: `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`"),
                                fieldWithPath("content[].createdAt").description("作成日時"),
                                fieldWithPath("content[].respondedAt").optional().description("応答日時 (未応答時は null)"),
                                fieldWithPath("page").description("現在のページ番号 (0から)"),
                                fieldWithPath("size").description("ページサイズ"),
                                fieldWithPath("totalElements").description("全件数"),
                                fieldWithPath("totalPages").description("全ページ数"))));
    }

    @Test
    @DisplayName("초대 취소 - OWNER, 204 No Content")
    void cancel() throws Exception {
        mockMvc.perform(post("/api/invitations/{invitationId}/cancel", 50L).with(owner()))
                .andExpect(status().isNoContent())
                .andDo(document("invitation-cancel",
                        pathParameters(parameterWithName("invitationId").description("キャンセルする招待ID"))));
    }

    @Test
    @DisplayName("받은 초대 목록 조회 - PART_TIMER (PENDING 상태만), 그룹명/초대한 점주 이름 포함")
    void received() throws Exception {
        given(getInvitationsQuery.getReceivedPendingInvitations(2L)).willReturn(List.of(
                new GetInvitationsQuery.ReceivedInvitation(
                        new Invitation(50L, 10L, 1L, 2L, InvitationStatus.PENDING,
                                LocalDateTime.of(2026, 5, 1, 9, 0), null),
                        "강남점", "박점주")));

        mockMvc.perform(get("/api/invitations/received").with(partTimer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[0].groupName").value("강남점"))
                .andExpect(jsonPath("$[0].inviterName").value("박점주"))
                .andDo(document("invitation-received",
                        responseFields(
                                fieldWithPath("[].id").description("招待ID"),
                                fieldWithPath("[].groupId").description("グループID"),
                                fieldWithPath("[].groupName").optional().description("グループ名 (照会失敗時は null)"),
                                fieldWithPath("[].inviterId").description("招待したオーナー会員ID"),
                                fieldWithPath("[].inviterName").optional().description("招待したオーナーの名前 (照会失敗時は null)"),
                                fieldWithPath("[].inviteeId").description("招待されたアルバイト会員ID"),
                                fieldWithPath("[].status").description("状態 (常に `PENDING`)"),
                                fieldWithPath("[].createdAt").description("作成日時"),
                                fieldWithPath("[].respondedAt").optional().description("応答日時 (未応答時は null)"))));
    }

    @Test
    @DisplayName("초대 수락 - PART_TIMER, 204 No Content")
    void accept() throws Exception {
        mockMvc.perform(post("/api/invitations/{invitationId}/accept", 50L).with(partTimer()))
                .andExpect(status().isNoContent())
                .andDo(document("invitation-accept",
                        pathParameters(parameterWithName("invitationId").description("承認する招待ID"))));
    }

    @Test
    @DisplayName("초대 거절 - PART_TIMER, 204 No Content")
    void reject() throws Exception {
        mockMvc.perform(post("/api/invitations/{invitationId}/reject", 50L).with(partTimer()))
                .andExpect(status().isNoContent())
                .andDo(document("invitation-reject",
                        pathParameters(parameterWithName("invitationId").description("拒否する招待ID"))));
    }
}
