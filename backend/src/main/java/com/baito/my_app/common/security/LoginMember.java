package com.baito.my_app.common.security;

import com.baito.my_app.member.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * The authenticated principal. This is what Spring Security stores in the SecurityContext,
 * which Spring Session persists to Redis — so it carries only {@code memberId}, {@code loginId}
 * and {@code role} (plus the password hash, used solely during authentication).
 *
 * <p>Volatile authorization facts (group ownership / membership) are deliberately NOT kept here;
 * they are re-checked against the DB on every request.
 */
public class LoginMember implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    private final Long memberId;
    private final String loginId;
    private final String password;
    private final Role role;

    public LoginMember(Long memberId, String loginId, String password, Role role) {
        this.memberId = memberId;
        this.loginId = loginId;
        this.password = password;
        this.role = role;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Role getRole() {
        return role;
    }

    public boolean isOwner() {
        return role == Role.OWNER;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return loginId;
    }
}
