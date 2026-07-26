package com.baito.my_app.common.security;

import com.baito.my_app.member.domain.Role;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * Issues and resolves opaque bearer tokens.
 *
 * <p>On login a cryptographically random token is generated and returned to the client; the
 * client's identity ({@link LoginMember}) is serialized to JSON, encrypted with {@link TokenCipher}
 * and stored in Redis under that token. Subsequent requests present the token in the
 * {@code Authorization: Bearer} header — the token itself carries no information, so it is safe to
 * expose, and the server can revoke it at any time by dropping the Redis entry.
 */
@Service
public class AuthTokenService {

    private static final int TOKEN_BYTES = 32;

    private final AuthTokenStore store;
    private final TokenCipher cipher;
    private final ObjectMapper objectMapper;
    private final AuthTokenProperties properties;

    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder tokenEncoder = Base64.getUrlEncoder().withoutPadding();

    public AuthTokenService(AuthTokenStore store,
                            TokenCipher cipher,
                            ObjectMapper objectMapper,
                            AuthTokenProperties properties) {
        this.store = store;
        this.cipher = cipher;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /** Issues a new token for the authenticated member and returns it to hand back to the client. */
    public String issue(LoginMember member) {
        String token = generateToken();
        String payload = objectMapper.writeValueAsString(TokenPayload.from(member));
        store.store(token, cipher.encrypt(payload), properties.getTtl());
        return token;
    }

    /** Resolves a bearer token back into a principal, or empty if unknown/expired/revoked. */
    public Optional<LoginMember> resolve(String token) {
        return store.find(token)
                .map(cipher::decrypt)
                .map(json -> objectMapper.readValue(json, TokenPayload.class))
                .map(TokenPayload::toLoginMember);
    }

    /** Revokes a token (logout); a no-op if it is unknown. */
    public void revoke(String token) {
        store.remove(token);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return tokenEncoder.encodeToString(bytes);
    }

    /** JSON shape persisted in Redis. Password is never stored — it is irrelevant after login. */
    record TokenPayload(Long memberId, String loginId, Role role) {

        static TokenPayload from(LoginMember member) {
            return new TokenPayload(member.getMemberId(), member.getUsername(), member.getRole());
        }

        LoginMember toLoginMember() {
            return new LoginMember(memberId, loginId, "", role);
        }
    }
}
