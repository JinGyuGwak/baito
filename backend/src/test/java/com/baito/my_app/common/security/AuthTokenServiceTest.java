package com.baito.my_app.common.security;

import com.baito.my_app.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthTokenServiceTest {

    private final AuthTokenStore store = mock(AuthTokenStore.class);
    private final TokenCipher cipher = new TokenCipher(devProperties());
    private final AuthTokenService service =
            new AuthTokenService(store, cipher, JsonMapper.builder().build(), devProperties());

    private static AuthTokenProperties devProperties() {
        AuthTokenProperties properties = new AuthTokenProperties();
        // Base64("0123456789abcdef0123456789abcdef") — 32-byte dev key.
        properties.setSecret("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        properties.setTtl(Duration.ofMinutes(30));
        return properties;
    }

    @Test
    @DisplayName("issue - 토큰을 발급하고 암호화된 회원 정보를 TTL과 함께 저장한다")
    void issue() {
        LoginMember member = new LoginMember(7L, "owner01", "irrelevant-hash", Role.OWNER);

        String token = service.issue(member);

        ArgumentCaptor<String> stored = ArgumentCaptor.forClass(String.class);
        verify(store).store(eq(token), stored.capture(), eq(Duration.ofMinutes(30)));
        // Stored value is ciphertext, not the raw login id.
        assertThat(stored.getValue()).doesNotContain("owner01");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("resolve - 저장된 토큰을 복호화해 회원 정보를 복원한다 (비밀번호는 포함하지 않음)")
    void resolve_roundTrip() {
        LoginMember member = new LoginMember(7L, "owner01", "irrelevant-hash", Role.OWNER);

        // Capture what issue() stores, then feed it back to resolve().
        String token = service.issue(member);
        ArgumentCaptor<String> stored = ArgumentCaptor.forClass(String.class);
        verify(store).store(eq(token), stored.capture(), any());
        given(store.find(token)).willReturn(Optional.of(stored.getValue()));

        LoginMember resolved = service.resolve(token).orElseThrow();

        assertThat(resolved.getMemberId()).isEqualTo(7L);
        assertThat(resolved.getUsername()).isEqualTo("owner01");
        assertThat(resolved.getRole()).isEqualTo(Role.OWNER);
        assertThat(resolved.getPassword()).isEmpty();
    }

    @Test
    @DisplayName("resolve - 알 수 없는 토큰이면 빈 값을 반환한다")
    void resolve_unknown() {
        given(store.find("nope")).willReturn(Optional.empty());

        assertThat(service.resolve("nope")).isEmpty();
    }

    @Test
    @DisplayName("revoke - 저장소에서 토큰을 삭제한다")
    void revoke() {
        service.revoke("some-token");

        verify(store).remove("some-token");
    }
}
