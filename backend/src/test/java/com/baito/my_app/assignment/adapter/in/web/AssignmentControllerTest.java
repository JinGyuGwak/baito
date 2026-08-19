package com.baito.my_app.assignment.adapter.in.web;

import com.baito.my_app.assignment.application.port.in.AssignShiftUseCase;
import com.baito.my_app.assignment.application.port.in.CancelShiftUseCase;
import com.baito.my_app.assignment.application.port.in.GetAssignmentCandidatesQuery;
import com.baito.my_app.assignment.application.port.in.GetAssignmentsQuery;
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
import static org.mockito.ArgumentMatchers.eq;
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
    private CancelShiftUseCase cancelShiftUseCase;
    @MockitoBean
    private GetAssignmentsQuery getAssignmentsQuery;
    @MockitoBean
    private GetAssignmentCandidatesQuery getAssignmentCandidatesQuery;

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
                        pathParameters(parameterWithName("groupId").description("グループID")),
                        requestFields(
                                fieldWithPath("memberId").description("割り当てるアルバイト会員"),
                                fieldWithPath("workDate").description("勤務日 (yyyy-MM-dd)"),
                                fieldWithPath("startTime").description("開始時刻 (HH:mm), 30分単位"),
                                fieldWithPath("endTime").description("終了時刻 (HH:mm), 30分単位")),
                        responseFields(
                                fieldWithPath("assignedSlotCount").description("新たに割り当てられた30分スロット数"))));
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
                                fieldWithPath("code").description("エラーコード: `STAFF_QUOTA_EXCEEDED`"),

                                fieldWithPath("message").description("エラーメッセージ"))));
    }

    @Test
    @DisplayName("근무 배정 취소 - 범위 내 확정 슬롯을 취소하고 취소된 슬롯 수 반환")
    void cancel() throws Exception {
        given(cancelShiftUseCase.cancel(any())).willReturn(2);

        String body = objectMapper.writeValueAsString(Map.of(
                "memberId", 2,
                "workDate", "2026-05-10",
                "startTime", "09:00",
                "endTime", "10:00"));

        mockMvc.perform(post("/api/groups/{groupId}/assignments/cancel", 10L).with(owner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelledSlotCount").value(2))
                .andDo(document("assignment-cancel",
                        pathParameters(parameterWithName("groupId").description("グループID")),
                        requestFields(
                                fieldWithPath("memberId").description("割り当てを解除するアルバイト会員ID"),
                                fieldWithPath("workDate").description("勤務日 (yyyy-MM-dd)"),
                                fieldWithPath("startTime").description("開始時刻 (HH:mm), 30分単位"),
                                fieldWithPath("endTime").description("終了時刻 (HH:mm), 30分単位")),
                        responseFields(
                                fieldWithPath("cancelledSlotCount").description("解除された30分スロット数 (確定した割り当てがなかったスロットは無視)"))));
    }

    @Test
    @DisplayName("근무 배정 목록 조회 - 특정 날짜의 확정 배정 (회원 이름 포함)")
    void getList() throws Exception {
        given(getAssignmentsQuery.getAssignments(10L, 1L, LocalDate.of(2026, 5, 10))).willReturn(List.of(
                new GetAssignmentsQuery.AssignmentDetail(2L, "김알바", "worker01", LocalTime.of(9, 0))));

        mockMvc.perform(get("/api/groups/{groupId}/assignments", 10L).with(owner())
                        .param("date", "2026-05-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(2))
                .andExpect(jsonPath("$[0].memberName").value("김알바"))
                .andDo(document("assignment-list",
                        pathParameters(parameterWithName("groupId").description("グループID")),
                        queryParameters(parameterWithName("date").description("照会日 (yyyy-MM-dd)")),
                        responseFields(
                                fieldWithPath("[].memberId").description("割り当てられたアルバイト会員ID"),
                                fieldWithPath("[].memberName").description("割り当てられたアルバイトの名前"),
                                fieldWithPath("[].memberLoginId").description("割り当てられたアルバイトのログインID"),
                                fieldWithPath("[].startTime").description("スロット開始時刻 (HH:mm:ss)"))));
    }

    @Test
    @DisplayName("배정 가능 알바생 조회 - 시간대 전체를 커버하는 멤버 목록, 배정 완료 여부 포함")
    void candidates() throws Exception {
        given(getAssignmentCandidatesQuery.getCandidates(
                eq(10L), eq(1L), eq(LocalDate.of(2026, 5, 10)), eq(LocalTime.of(9, 0)), eq(LocalTime.of(12, 0))))
                .willReturn(List.of(
                        new GetAssignmentCandidatesQuery.Candidate(2L, "김알바", "worker01",
                                List.of(new GetAssignmentCandidatesQuery.Interval(LocalTime.of(9, 0), LocalTime.of(15, 0))),
                                false),
                        new GetAssignmentCandidatesQuery.Candidate(3L, "이알바", "worker02",
                                List.of(new GetAssignmentCandidatesQuery.Interval(LocalTime.of(8, 0), LocalTime.of(12, 0))),
                                true)));

        mockMvc.perform(get("/api/groups/{groupId}/assignments/candidates", 10L).with(owner())
                        .param("date", "2026-05-10")
                        .param("startTime", "09:00")
                        .param("endTime", "12:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(2))
                .andExpect(jsonPath("$[0].alreadyAssigned").value(false))
                .andExpect(jsonPath("$[1].alreadyAssigned").value(true))
                .andDo(document("assignment-candidates",
                        pathParameters(parameterWithName("groupId").description("グループID")),
                        queryParameters(
                                parameterWithName("date").description("勤務日 (yyyy-MM-dd)"),
                                parameterWithName("startTime").description("時間帯の開始 (HH:mm), 30分単位"),
                                parameterWithName("endTime").description("時間帯の終了 (HH:mm), 30分単位")),
                        responseFields(
                                fieldWithPath("[].memberId").description("アルバイト会員ID"),
                                fieldWithPath("[].name").description("アルバイトの名前"),
                                fieldWithPath("[].loginId").description("アルバイトのログインID"),
                                fieldWithPath("[].availableIntervals").description("該当日の勤務可能時間帯リスト"),
                                fieldWithPath("[].availableIntervals[].startTime").description("勤務可能区間の開始 (HH:mm:ss)"),
                                fieldWithPath("[].availableIntervals[].endTime").description("勤務可能区間の終了 (HH:mm:ss)"),
                                fieldWithPath("[].alreadyAssigned").description("選択した時間帯すべてに既に割り当て済みの場合 `true` (選択不可として表示)"))));
    }
}
