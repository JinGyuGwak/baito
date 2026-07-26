package com.baito.my_app.member.application.service;

import com.baito.my_app.member.application.port.in.SignUpUseCase;
import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.application.port.out.PasswordEncryptor;
import com.baito.my_app.member.domain.DuplicateLoginIdException;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncryptor passwordEncryptor;
    @InjectMocks
    private SignUpService service;

    private SignUpUseCase.Command command() {
        return new SignUpUseCase.Command("owner01", "password123!", "홍길동", Role.OWNER);
    }

    @Test
    @DisplayName("가입 성공 - 비밀번호를 암호화해 저장하고 생성된 id를 반환한다")
    void signUp() {
        given(memberRepository.existsByLoginId("owner01")).willReturn(false);
        given(passwordEncryptor.encode("password123!")).willReturn("hashed-pw");
        given(memberRepository.save(any())).willAnswer(inv -> {
            Member m = inv.getArgument(0);
            return new Member(100L, m.getLoginId(), m.getPassword(), m.getName(), m.getRole(), null);
        });

        Long id = service.signUp(command());

        assertThat(id).isEqualTo(100L);
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        Member saved = captor.getValue();
        assertThat(saved.getLoginId()).isEqualTo("owner01");
        assertThat(saved.getPassword()).isEqualTo("hashed-pw"); // 원문이 아닌 해시가 저장돼야 한다
        assertThat(saved.getRole()).isEqualTo(Role.OWNER);
    }

    @Test
    @DisplayName("가입 실패 - 로그인 ID가 이미 존재하면 저장하지 않고 예외를 던진다")
    void signUp_duplicateLoginId() {
        given(memberRepository.existsByLoginId("owner01")).willReturn(true);

        assertThatThrownBy(() -> service.signUp(command()))
                .isInstanceOf(DuplicateLoginIdException.class);

        verify(memberRepository, never()).save(any());
        verify(passwordEncryptor, never()).encode(any());
    }
}
