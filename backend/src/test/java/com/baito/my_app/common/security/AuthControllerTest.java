package com.baito.my_app.common.security;

import com.baito.my_app.member.domain.Role;
import com.baito.my_app.support.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends RestDocsSupport {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인 성공 - 세션 생성 후 회원 정보 반환")
    void login() throws Exception {
        LoginMember stored = new LoginMember(1L, "owner01", passwordEncoder.encode("password123!"), Role.OWNER);
        given(memberUserDetailsService.loadUserByUsername("owner01")).willReturn(stored);

        String body = objectMapper.writeValueAsString(Map.of(
                "loginId", "owner01",
                "password", "password123!"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.loginId").value("owner01"))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andDo(document("auth-login",
                        requestFields(
                                fieldWithPath("loginId").description("로그인 ID"),
                                fieldWithPath("password").description("비밀번호")),
                        responseFields(
                                fieldWithPath("memberId").description("회원 ID"),
                                fieldWithPath("loginId").description("로그인 ID"),
                                fieldWithPath("role").description("역할: `OWNER` 또는 `PART_TIMER`"))));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 자격증명 시 401 Unauthorized")
    void login_badCredentials() throws Exception {
        LoginMember stored = new LoginMember(1L, "owner01", passwordEncoder.encode("password123!"), Role.OWNER);
        given(memberUserDetailsService.loadUserByUsername("owner01")).willReturn(stored);

        String body = objectMapper.writeValueAsString(Map.of(
                "loginId", "owner01",
                "password", "wrong-password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"))
                .andDo(document("auth-login-failed",
                        responseFields(
                                fieldWithPath("code").description("에러 코드: `AUTHENTICATION_FAILED`"),
                                fieldWithPath("message").description("에러 메시지"))));
    }

    @Test
    @DisplayName("내 정보 조회 - 인증된 회원 정보 반환")
    void me() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(owner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andDo(document("auth-me",
                        responseFields(
                                fieldWithPath("memberId").description("회원 ID"),
                                fieldWithPath("loginId").description("로그인 ID"),
                                fieldWithPath("role").description("역할"))));
    }
}
