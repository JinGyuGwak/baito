package com.baito.my_app.member.adapter.in.web;

import com.baito.my_app.member.application.port.in.SignUpUseCase;
import com.baito.my_app.member.domain.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
                request.loginId(),
                request.password(),
                request.name(),
                request.role()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(new SignUpResponse(memberId));
    }

    public record SignUpRequest(
            @NotBlank @Size(max = 50) String loginId,
            @NotBlank @Size(min = 8, max = 64) String password,
            @NotBlank @Size(max = 50) String name,
            @NotNull Role role
    ) {
    }

    public record SignUpResponse(Long memberId) {
    }
}
