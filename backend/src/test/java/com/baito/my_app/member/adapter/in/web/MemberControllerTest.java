package com.baito.my_app.member.adapter.in.web;

import com.baito.my_app.member.application.port.in.MemberProfileUseCase;
import com.baito.my_app.member.application.port.in.SignUpUseCase;
import com.baito.my_app.member.domain.Member;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest extends RestDocsSupport {

    @MockitoBean
    private SignUpUseCase signUpUseCase;
    @MockitoBean
    private MemberProfileUseCase memberProfileUseCase;

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
                                fieldWithPath("loginId").description("ログインID (最大50文字, 重複不可)"),
                                fieldWithPath("password").description("パスワード (8〜64文字)"),
                                fieldWithPath("name").description("名前 (最大50文字)"),
                                fieldWithPath("role").description("役割: `OWNER`(オーナー) または `PART_TIMER`(アルバイト)")),
                        responseFields(
                                fieldWithPath("memberId").description("作成された会員ID"))));
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
                                fieldWithPath("code").description("エラーコード: `VALIDATION_ERROR`"),
                                fieldWithPath("message").description("検証失敗の詳細メッセージ"))));
    }

    @Test
    @DisplayName("내 프로필 조회 - 로그인한 회원의 loginId/이름/역할 반환")
    void myProfile() throws Exception {
        given(memberProfileUseCase.getMyProfile(1L)).willReturn(
                new Member(1L, "owner01", "", "홍길동", Role.OWNER, null));

        mockMvc.perform(get("/api/members/me").with(owner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("owner01"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andDo(document("member-me",
                        responseFields(
                                fieldWithPath("memberId").description("会員ID"),
                                fieldWithPath("loginId").description("ログインID (変更不可)"),
                                fieldWithPath("name").description("名前"),
                                fieldWithPath("role").description("役割: `OWNER` または `PART_TIMER` (変更不可)"))));
    }

    @Test
    @DisplayName("내 이름 변경 - 변경된 프로필 반환")
    void updateName() throws Exception {
        given(memberProfileUseCase.changeName(eq(1L), eq("김철수"))).willReturn(
                new Member(1L, "owner01", "", "김철수", Role.OWNER, null));

        String body = objectMapper.writeValueAsString(Map.of("name", "김철수"));

        mockMvc.perform(patch("/api/members/me").with(owner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("김철수"))
                .andDo(document("member-update-name",
                        requestFields(
                                fieldWithPath("name").description("変更する名前 (1〜50文字)")),
                        responseFields(
                                fieldWithPath("memberId").description("会員ID"),
                                fieldWithPath("loginId").description("ログインID"),
                                fieldWithPath("name").description("変更された名前"),
                                fieldWithPath("role").description("役割"))));
    }

    @Test
    @DisplayName("내 이름 변경 실패 - 빈 이름은 400")
    void updateName_blank() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", ""));

        mockMvc.perform(patch("/api/members/me").with(owner())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
