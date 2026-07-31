package com.baito.my_app.member.adapter.out.security;

import com.baito.my_app.member.application.port.out.PasswordEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bridges the domain's {@link PasswordEncryptor} port to Spring Security's {@link PasswordEncoder}
 * (a BCrypt encoder, configured in SecurityConfig).
 */
@Component
public class PasswordEncoderAdapter implements PasswordEncryptor {

    private final PasswordEncoder passwordEncoder;

    public PasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
