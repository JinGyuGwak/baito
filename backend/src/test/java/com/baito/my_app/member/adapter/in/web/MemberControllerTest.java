package com.baito.my_app.member.adapter.in.web;

import com.baito.my_app.member.application.port.in.SignUpUseCase;
import com.baito.my_app.member.domain.Role;
import com.baito.my_app.support.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest extends RestDocsSupport {

    @MockitoBean
    private SignUpUseCase signUpUseCase;

    @Test
    @DisplayName("회원 가입 성공 - 201 Created")
    void signUp() throws Exception {
        given(signUpUseCase.signUp(any())).willReturn(100L);

        String body = objectMapper.writeValueAsString(Map.of(
                "loginId", "owner01",
                "password", "password123!",
                "name", "홍길동",
                "role", Role.OWNER));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(100))
                .andDo(document("member-sign-up",
                        requestFields(
                                fieldWithPath("loginId").description("로그인 ID (최대 50자, 중복 불가)"),
                                fieldWithPath("password").description("비밀번호 (8~64자)"),
                                fieldWithPath("name").description("이름 (최대 50자)"),
                                fieldWithPath("role").description("역할: `OWNER`(사장) 또는 `PART_TIMER`(알바)")),
                        responseFields(
                                fieldWithPath("memberId").description("생성된 회원 ID"))));
    }

    @Test
    @DisplayName("회원 가입 실패 - 필수값 누락 시 400 Bad Request")
    void signUp_validationError() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "loginId", "",
                "password", "short",
                "name", "홍길동",
                "role", Role.OWNER));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andDo(document("member-sign-up-validation-error",
                        responseFields(
                                fieldWithPath("code").description("에러 코드: `VALIDATION_ERROR`"),
                                fieldWithPath("message").description("검증 실패 상세 메시지"))));
    }
}
