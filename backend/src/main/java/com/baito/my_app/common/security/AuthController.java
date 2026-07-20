package com.baito.my_app.common.security;

import com.baito.my_app.member.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public MemberInfo login(@Valid @RequestBody LoginRequest request,
                            HttpServletRequest httpRequest,
                            HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.loginId(), request.password()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        // Writes the context into the (Redis-backed) HttpSession so subsequent requests are authenticated.
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return MemberInfo.from((LoginMember) authentication.getPrincipal());
    }

    @GetMapping("/me")
    public MemberInfo me(@AuthenticationPrincipal LoginMember loginMember) {
        return MemberInfo.from(loginMember);
    }

    public record LoginRequest(@NotBlank String loginId, @NotBlank String password) {
    }

    public record MemberInfo(Long memberId, String loginId, Role role) {
        static MemberInfo from(LoginMember member) {
            return new MemberInfo(member.getMemberId(), member.getUsername(), member.getRole());
        }
    }
}
