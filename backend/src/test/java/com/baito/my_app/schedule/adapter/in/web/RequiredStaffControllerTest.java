package com.baito.my_app.schedule.adapter.in.web;

import com.baito.my_app.schedule.application.port.in.GetScheduleQuery;
import com.baito.my_app.schedule.application.port.in.SetRequiredStaffUseCase;
import com.baito.my_app.schedule.domain.RequiredStaffSlot;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequiredStaffController.class)
class RequiredStaffControllerTest extends RestDocsSupport {

    @MockitoBean
    private SetRequiredStaffUseCase setRequiredStaffUseCase;
    @MockitoBean
    private GetScheduleQuery getScheduleQuery;

    @Test
    @DisplayName("필요 인원 설정 - OWNER, 해당 날짜 전체 교체, 204 No Content")
    void set() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "workDate", "2026-05-10",
                "intervals", List.of(
                        Map.of("startTime", "09:00", "endTime", "12:00", "requiredCount", 2),
                        Map.of("startTime", "12:00", "endTime", "14:00", "requiredCount", 3))));

        mockMvc.perform(put("/api/groups/{groupId}/required-staff", 10L).with(owner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent())
                .andDo(document("required-staff-set",
                        pathParameters(parameterWithName("groupId").description("그룹 ID")),
                        requestFields(
                                fieldWithPath("workDate").description("근무 날짜 (yyyy-MM-dd)"),
                                fieldWithPath("intervals").description("필요 인원 구간 목록 (기존 값 전체 교체)"),
                                fieldWithPath("intervals[].startTime").description("구간 시작 시각 (HH:mm)"),
                                fieldWithPath("intervals[].endTime").description("구간 종료 시각 (HH:mm)"),
                                fieldWithPath("intervals[].requiredCount").description("필요 인원 수 (0 이상)"))));
    }

    @Test
    @DisplayName("필요 인원 조회 - 30분 슬롯별 필요 인원")
    void getList() throws Exception {
        given(getScheduleQuery.getRequiredStaff(10L, 1L, LocalDate.of(2026, 5, 10))).willReturn(List.of(
                RequiredStaffSlot.of(10L, LocalDate.of(2026, 5, 10), LocalTime.of(9, 0), 2),
                RequiredStaffSlot.of(10L, LocalDate.of(2026, 5, 10), LocalTime.of(9, 30), 2)));

        mockMvc.perform(get("/api/groups/{groupId}/required-staff", 10L).with(owner())
                        .param("date", "2026-05-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requiredCount").value(2))
                .andDo(document("required-staff-get",
                        pathParameters(parameterWithName("groupId").description("그룹 ID")),
                        queryParameters(parameterWithName("date").description("조회 날짜 (yyyy-MM-dd)")),
                        responseFields(
                                fieldWithPath("[].startTime").description("30분 슬롯 시작 시각 (HH:mm:ss)"),
                                fieldWithPath("[].requiredCount").description("해당 슬롯 필요 인원 수"))));
    }
}
