package com.baito.my_app.assignment.adapter.in.web;

import com.baito.my_app.assignment.application.port.in.GetAssignmentsQuery;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.assignment.domain.ShiftAssignmentStatus;
import com.baito.my_app.support.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyAssignmentController.class)
class MyAssignmentControllerTest extends RestDocsSupport {

    @MockitoBean
    private GetAssignmentsQuery getAssignmentsQuery;

    @Test
    @DisplayName("내 확정 근무 조회 - 알바 본인의 확정 배정을 날짜별로 (전체 그룹)")
    void getMySchedule() throws Exception {
        given(getAssignmentsQuery.getMyAssignments(PART_TIMER.getMemberId(), LocalDate.of(2026, 5, 10)))
                .willReturn(List.of(
                        new ShiftAssignment(1L, 10L, PART_TIMER.getMemberId(), LocalDate.of(2026, 5, 10),
                                LocalTime.of(9, 0), 1L, ShiftAssignmentStatus.CONFIRMED, null)));

        mockMvc.perform(get("/api/me/assignments").with(partTimer())
                        .param("date", "2026-05-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupId").value(10))
                .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                .andDo(document("my-assignment-list",
                        queryParameters(parameterWithName("date").description("조회 날짜 (yyyy-MM-dd)")),
                        responseFields(
                                fieldWithPath("[].groupId").description("배정된 그룹 ID"),
                                fieldWithPath("[].startTime").description("슬롯 시작 시각 (HH:mm:ss)"))));
    }

    @Test
    @DisplayName("내 확정 근무 조회 실패 - 사장 권한으로 접근 시 403 Forbidden")
    void getMySchedule_forbiddenForOwner() throws Exception {
        mockMvc.perform(get("/api/me/assignments").with(owner())
                        .param("date", "2026-05-10"))
                .andExpect(status().isForbidden());
    }
}
