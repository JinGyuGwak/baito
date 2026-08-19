package com.baito.my_app.membership.adapter.in.web;

import com.baito.my_app.group.domain.WorkGroup;
import com.baito.my_app.membership.application.port.in.GetJoinedGroupsQuery;
import com.baito.my_app.support.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MembershipController.class)
class MembershipControllerTest extends RestDocsSupport {

    @MockitoBean
    private GetJoinedGroupsQuery getJoinedGroupsQuery;

    @Test
    @DisplayName("가입한 그룹 목록 조회 - PART_TIMER가 소속된 그룹")
    void myGroups() throws Exception {
        given(getJoinedGroupsQuery.getJoinedGroups(2L)).willReturn(List.of(
                new WorkGroup(10L, 1L, "강남점", "강남역 1호점", null)));

        mockMvc.perform(get("/api/memberships/groups").with(partTimer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andDo(document("membership-groups",
                        responseFields(
                                fieldWithPath("[].id").description("グループID"),
                                fieldWithPath("[].name").description("グループ名"),
                                fieldWithPath("[].description").description("説明"))));
    }
}
