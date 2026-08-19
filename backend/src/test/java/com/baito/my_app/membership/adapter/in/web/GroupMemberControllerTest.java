package com.baito.my_app.membership.adapter.in.web;

import com.baito.my_app.membership.application.port.in.GetGroupMembersQuery;
import com.baito.my_app.membership.application.port.in.RemoveMemberUseCase;
import com.baito.my_app.membership.domain.NotGroupMemberException;
import com.baito.my_app.support.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupMemberController.class)
class GroupMemberControllerTest extends RestDocsSupport {

    @MockitoBean
    private GetGroupMembersQuery getGroupMembersQuery;
    @MockitoBean
    private RemoveMemberUseCase removeMemberUseCase;

    @Test
    @DisplayName("그룹 멤버 목록 조회 - OWNER, 소속 알바생 이름/loginId 포함")
    void members() throws Exception {
        given(getGroupMembersQuery.getGroupMembers(10L, 1L)).willReturn(List.of(
                new GetGroupMembersQuery.GroupMember(2L, "김알바", "worker01",
                        LocalDateTime.of(2026, 5, 1, 9, 0))));

        mockMvc.perform(get("/api/groups/{groupId}/members", 10L).with(owner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(2))
                .andExpect(jsonPath("$[0].name").value("김알바"))
                .andDo(document("group-member-list",
                        pathParameters(parameterWithName("groupId").description("グループID")),
                        responseFields(
                                fieldWithPath("[].memberId").description("アルバイト会員ID"),
                                fieldWithPath("[].name").description("アルバイトの名前"),
                                fieldWithPath("[].loginId").description("アルバイトのログインID"),
                                fieldWithPath("[].joinedAt").description("グループ参加日時"))));
    }

    @Test
    @DisplayName("그룹 멤버 추방 - OWNER, 204 No Content")
    void remove() throws Exception {
        mockMvc.perform(delete("/api/groups/{groupId}/members/{memberId}", 10L, 2L).with(owner()))
                .andExpect(status().isNoContent())
                .andDo(document("group-member-remove",
                        pathParameters(
                                parameterWithName("groupId").description("グループID"),
                                parameterWithName("memberId").description("削除するアルバイト会員ID"))));
    }

    @Test
    @DisplayName("그룹 멤버 추방 실패 - 활성 멤버가 아니면 403")
    void remove_notMember() throws Exception {
        BDDMockito.willThrow(new NotGroupMemberException(10L))
                .given(removeMemberUseCase).remove(any());

        mockMvc.perform(delete("/api/groups/{groupId}/members/{memberId}", 10L, 999L).with(owner()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_GROUP_MEMBER"))
                .andDo(document("group-member-remove-not-member",
                        responseFields(
                                fieldWithPath("code").description("エラーコード: `NOT_GROUP_MEMBER`"),
                                fieldWithPath("message").description("エラーメッセージ"))));
    }

    @Test
    @DisplayName("그룹 멤버 목록 조회 실패 - PART_TIMER는 403")
    void members_forbiddenForPartTimer() throws Exception {
        mockMvc.perform(get("/api/groups/{groupId}/members", 10L).with(partTimer()))
                .andExpect(status().isForbidden());
    }
}
