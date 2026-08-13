package com.baito.my_app.schedule.adapter.in.web;

import com.baito.my_app.schedule.application.port.in.GetScheduleQuery;
import com.baito.my_app.schedule.application.port.in.SetAvailabilityUseCase;
import com.baito.my_app.schedule.domain.AvailabilitySlot;
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

@WebMvcTest(AvailabilityController.class)
class AvailabilityControllerTest extends RestDocsSupport {

    @MockitoBean
    private SetAvailabilityUseCase setAvailabilityUseCase;
    @MockitoBean
    private GetScheduleQuery getScheduleQuery;

    @Test
    @DisplayName("근무 가능 시간 설정 - PART_TIMER, 해당 날짜 전체 교체, 204 No Content")
    void set() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "workDate", "2026-05-10",
                "intervals", List.of(
                        Map.of("startTime", "09:00", "endTime", "12:00"),
                        Map.of("startTime", "13:00", "endTime", "18:00"))));

        mockMvc.perform(put("/api/groups/{groupId}/availability", 10L).with(partTimer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent())
                .andDo(document("availability-set",
                        pathParameters(parameterWithName("groupId").description("グループID")),
                        requestFields(
                                fieldWithPath("workDate").description("勤務日 (yyyy-MM-dd)"),
                                fieldWithPath("intervals").description("勤務可能時間の区間リスト (既存の値を全て置換)"),
                                fieldWithPath("intervals[].startTime").description("区間の開始時刻 (HH:mm)"),
                                fieldWithPath("intervals[].endTime").description("区間の終了時刻 (HH:mm)"))));
    }

    @Test
    @DisplayName("근무 가능 시간 조회 - 30분 슬롯 시작 시각 목록")
    void getList() throws Exception {
        given(getScheduleQuery.getMyAvailability(10L, 2L, LocalDate.of(2026, 5, 10))).willReturn(List.of(
                AvailabilitySlot.of(10L, 2L, LocalDate.of(2026, 5, 10), LocalTime.of(9, 0)),
                AvailabilitySlot.of(10L, 2L, LocalDate.of(2026, 5, 10), LocalTime.of(9, 30))));

        mockMvc.perform(get("/api/groups/{groupId}/availability", 10L).with(partTimer())
                        .param("date", "2026-05-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                .andDo(document("availability-get",
                        pathParameters(parameterWithName("groupId").description("グループID")),
                        queryParameters(parameterWithName("date").description("照会日 (yyyy-MM-dd)")),
                        responseFields(
                                fieldWithPath("[].startTime").description("勤務可能な30分スロットの開始時刻 (HH:mm:ss)"))));
    }
}
