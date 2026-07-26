package com.baito.my_app.member.adapter.in.web;

import com.baito.my_app.member.application.port.in.SignUpUseCase;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final SignUpUseCase signUpUseCase;

    public MemberController(SignUpUseCase signUpUseCase) {
        this.signUpUseCase = signUpUseCase;
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
}
