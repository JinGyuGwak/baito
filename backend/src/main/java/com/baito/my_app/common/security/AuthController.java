package com.baito.my_app.common.security;

import com.baito.my_app.member.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthTokenService authTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          AuthTokenService authTokenService) {
        this.authenticationManager = authenticationManager;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLoginId(), request.getPassword()));

        LoginMember member = (LoginMember) authentication.getPrincipal();
        String token = authTokenService.issue(member);
        return LoginResponse.of(token, member);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        String token = BearerTokenAuthenticationFilter.resolveToken(httpRequest);
        if (token != null) {
            authTokenService.revoke(token);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public MemberInfo me(@AuthenticationPrincipal LoginMember loginMember) {
        return MemberInfo.from(loginMember);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank
        private String loginId;
        @NotBlank
        private String password;
    }

    @Getter
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private Long memberId;
        private String loginId;
        private Role role;

        static LoginResponse of(String token, LoginMember member) {
            return new LoginResponse(token, member.getMemberId(), member.getUsername(), member.getRole());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class MemberInfo {
        private Long memberId;
        private String loginId;
        private Role role;

        static MemberInfo from(LoginMember member) {
            return new MemberInfo(member.getMemberId(), member.getUsername(), member.getRole());
        }
    }
}
