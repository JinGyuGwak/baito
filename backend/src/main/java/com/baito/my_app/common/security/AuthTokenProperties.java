package com.baito.my_app.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration for the opaque bearer-token authentication scheme.
 *
 * @see AuthTokenService
 */
@ConfigurationProperties(prefix = "app.auth.token")
public class AuthTokenProperties {

    /** Base64-encoded 256-bit (32-byte) AES key used to encrypt the principal stored in Redis. */
    private String secret;

    /** How long an issued token stays valid in Redis; refreshed is not automatic. */
    private Duration ttl = Duration.ofMinutes(30);

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }
}
