package com.baito.my_app.member.application.service;

import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.member.domain.MemberNotFoundException;
import com.baito.my_app.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private MemberProfileService service;

    @Test
    @DisplayName("내 프로필 조회 - 회원을 그대로 반환한다")
    void getMyProfile() {
        Member member = new Member(1L, "owner01", "", "홍길동", Role.OWNER, null);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        assertThat(service.getMyProfile(1L)).isEqualTo(member);
    }

    @Test
    @DisplayName("내 프로필 조회 실패 - 회원이 없으면 MemberNotFoundException")
    void getMyProfile_notFound() {
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyProfile(1L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("이름 변경 - 이름을 바꿔 저장하고 저장 결과를 반환한다")
    void changeName() {
        Member member = new Member(1L, "owner01", "", "홍길동", Role.OWNER, null);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        Member updated = service.changeName(1L, "김철수");

        assertThat(updated.getName()).isEqualTo("김철수");
        ArgumentCaptor<Member> captor = ArgumentCaptor.captor();
        then(memberRepository).should().save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("김철수");
        assertThat(captor.getValue().getLoginId()).isEqualTo("owner01"); // loginId는 그대로
    }

    @Test
    @DisplayName("이름 변경 실패 - 회원이 없으면 MemberNotFoundException")
    void changeName_notFound() {
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeName(1L, "김철수"))
                .isInstanceOf(MemberNotFoundException.class);
    }
}
