package com.baito.my_app.member.application.port.out;

/**
 * Outbound port that hides the concrete password hashing mechanism (BCrypt) from the domain,
 * keeping the sign-up service free of any Spring Security dependency.
 */
public interface PasswordEncryptor {

    String encode(String rawPassword);
}
