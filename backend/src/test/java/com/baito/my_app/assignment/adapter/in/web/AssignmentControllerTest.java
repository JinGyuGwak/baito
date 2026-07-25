package com.baito.my_app.assignment.adapter.in.web;

import com.baito.my_app.assignment.application.port.in.AssignShiftUseCase;
import com.baito.my_app.assignment.application.port.in.GetAssignmentsQuery;
import com.baito.my_app.assignment.domain.ShiftAssignment;
import com.baito.my_app.assignment.domain.ShiftAssignmentStatus;
import com.baito.my_app.assignment.domain.StaffQuotaExceededException;
import com.baito.my_app.support.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
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

@WebMvcTest(AssignmentController.class)
class AssignmentControllerTest extends RestDocsSupport {

    @MockitoBean
    private AssignShiftUseCase assignShiftUseCase;
    @MockitoBean
    private GetAssignmentsQuery getAssignmentsQuery;

    @Test
    @DisplayName("근무 배정 성공 - 시간 범위를 30분 슬롯으로 나눠 배정, 배정된 슬롯 수 반환")
    void assign() throws Exception {
        given(assignShiftUseCase.assign(any())).willReturn(4);

        String body = objectMapper.writeValueAsString(Map.of(
                "memberId", 2,
                "workDate", "2026-05-10",
                "startTime", "09:00",
                "endTime", "11:00"));

        mockMvc.perform(post("/api/groups/{groupId}/assignments", 10L).with(owner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedSlotCount").value(4))
                .andDo(document("assignment-assign",
                        pathParameters(parameterWithName("groupId").description("그룹 ID")),
                        requestFields(
                                fieldWithPath("memberId").description("배정할 알바 회원"),
                                fieldWithPath("workDate").description("근무 날짜 (yyyy-MM-dd)"),
                                fieldWithPath("startTime").description("시작 시각 (HH:mm), 30분 단위"),
                                fieldWithPath("endTime").description("종료 시각 (HH:mm), 30분 단위")),
                        responseFields(
                                fieldWithPath("assignedSlotCount").description("새로 배정된 30분 슬롯 수"))));
    }

    @Test
    @DisplayName("근무 배정 실패 - 필요 인원 초과 시 409 Conflict")
    void assign_quotaExceeded() throws Exception {
        given(assignShiftUseCase.assign(any()))
                .willThrow(new StaffQuotaExceededException(LocalDate.of(2026, 5, 10), LocalTime.of(9, 0), 1));

        String body = objectMapper.writeValueAsString(Map.of(
                "memberId", 2,
                "workDate", "2026-05-10",
                "startTime", "09:00",
                "endTime", "09:30"));

        mockMvc.perform(post("/api/groups/{groupId}/assignments", 10L).with(owner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STAFF_QUOTA_EXCEEDED"))
                .andDo(document("assignment-assign-quota-exceeded",
                        responseFields(
                                fieldWithPath("code").description("에러 코드: `STAFF_QUOTA_EXCEEDED`"),

                                fieldWithPath("message").description("에러 메시지"))));
    }

    @Test
    @DisplayName("근무 배정 목록 조회 - 특정 날짜의 확정 배정")
    void getList() throws Exception {
        given(getAssignmentsQuery.getAssignments(10L, 1L, LocalDate.of(2026, 5, 10))).willReturn(List.of(
                new ShiftAssignment(1L, 10L, 2L, LocalDate.of(2026, 5, 10), LocalTime.of(9, 0),
                        1L, ShiftAssignmentStatus.CONFIRMED, null)));

        mockMvc.perform(get("/api/groups/{groupId}/assignments", 10L).with(owner())
                        .param("date", "2026-05-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(2))
                .andDo(document("assignment-list",
                        pathParameters(parameterWithName("groupId").description("그룹 ID")),
                        queryParameters(parameterWithName("date").description("조회 날짜 (yyyy-MM-dd)")),
                        responseFields(
                                fieldWithPath("[].memberId").description("배정된 알바 회원 ID"),
                                fieldWithPath("[].startTime").description("슬롯 시작 시각 (HH:mm:ss)"))));
    }
}
