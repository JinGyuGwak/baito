package com.baito.my_app.member.application.port.in;

import com.baito.my_app.member.domain.Role;

/**
 * Inbound port: register a new member with a fixed role.
 */
public interface SignUpUseCase {

    Long signUp(Command command);

    record Command(String loginId, String rawPassword, String name, Role role) {
    }
}
