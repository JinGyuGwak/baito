package com.baito.my_app.group.adapter.in.web;

import com.baito.my_app.group.application.port.in.CreateGroupUseCase;
import com.baito.my_app.group.application.port.in.GetOwnedGroupsQuery;
import com.baito.my_app.group.domain.WorkGroup;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
class GroupControllerTest extends RestDocsSupport {

    @MockitoBean
    private CreateGroupUseCase createGroupUseCase;
    @MockitoBean
    private GetOwnedGroupsQuery getOwnedGroupsQuery;

    @Test
    @DisplayName("그룹 생성 성공 - OWNER 권한, 201 Created")
    void create() throws Exception {
        given(createGroupUseCase.createGroup(any())).willReturn(10L);

        String body = objectMapper.writeValueAsString(Map.of(
                "name", "강남점",
                "description", "강남역 1호점"));

        mockMvc.perform(post("/api/groups").with(owner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupId").value(10))
                .andDo(document("group-create",
                        requestFields(
                                fieldWithPath("name").description("グループ(店舗)名 (最大100文字)"),
                                fieldWithPath("description").optional().description("説明 (最大255文字, 任意)")),
                        responseFields(
                                fieldWithPath("groupId").description("作成されたグループID"))));
    }

    @Test
    @DisplayName("그룹 생성 실패 - PART_TIMER 권한은 403 Forbidden")
    void create_forbiddenForPartTimer() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "강남점"));

        mockMvc.perform(post("/api/groups").with(partTimer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("내 그룹 목록 조회 - OWNER가 소유한 그룹 목록")
    void myGroups() throws Exception {
        given(getOwnedGroupsQuery.getOwnedGroups(1L)).willReturn(List.of(
                new WorkGroup(10L, 1L, "강남점", "강남역 1호점", LocalDateTime.of(2026, 5, 1, 9, 0))));

        mockMvc.perform(get("/api/groups").with(owner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andDo(document("group-list",
                        responseFields(
                                fieldWithPath("[].id").description("グループID"),
                                fieldWithPath("[].name").description("グループ名"),
                                fieldWithPath("[].description").description("説明"),
                                fieldWithPath("[].createdAt").description("作成日時 (ISO-8601)"))));
    }
}
