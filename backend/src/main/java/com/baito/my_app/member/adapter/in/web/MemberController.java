package com.baito.my_app.member.adapter.in.web;

import com.baito.my_app.common.security.LoginMember;
import com.baito.my_app.member.application.port.in.MemberProfileUseCase;
import com.baito.my_app.member.application.port.in.SignUpUseCase;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.member.domain.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final SignUpUseCase signUpUseCase;
    private final MemberProfileUseCase memberProfileUseCase;

    public MemberController(SignUpUseCase signUpUseCase, MemberProfileUseCase memberProfileUseCase) {
        this.signUpUseCase = signUpUseCase;
        this.memberProfileUseCase = memberProfileUseCase;
    }

    @PostMapping
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        Long memberId = signUpUseCase.signUp(new SignUpUseCase.Command(
                request.getLoginId(),
                request.getPassword(),
                request.getName(),
                request.getRole()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(new SignUpResponse(memberId));
    }

    /** 내 프로필 조회 — 로그인 필요. */
    @GetMapping("/me")
    public MyProfileResponse me(@AuthenticationPrincipal LoginMember loginMember) {
        return MyProfileResponse.from(memberProfileUseCase.getMyProfile(loginMember.getMemberId()));
    }

    /** 내 이름 변경 — 로그인 필요. loginId/role은 변경 불가. */
    @PatchMapping("/me")
    public MyProfileResponse updateName(@Valid @RequestBody UpdateNameRequest request,
                                        @AuthenticationPrincipal LoginMember loginMember) {
        return MyProfileResponse.from(
                memberProfileUseCase.changeName(loginMember.getMemberId(), request.getName()));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class SignUpRequest {
        @NotBlank
        @Size(max = 50)
        private String loginId;
        @NotBlank
        @Size(min = 8, max = 64)
        private String password;
        @NotBlank
        @Size(max = 50)
        private String name;
        @NotNull
        private Role role;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class SignUpResponse {
        private Long memberId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UpdateNameRequest {
        @NotBlank
        @Size(max = 50)
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class MyProfileResponse {
        private Long memberId;
        private String loginId;
        private String name;
        private Role role;

        static MyProfileResponse from(Member member) {
            return new MyProfileResponse(member.getId(), member.getLoginId(), member.getName(), member.getRole());
        }
    }
}
