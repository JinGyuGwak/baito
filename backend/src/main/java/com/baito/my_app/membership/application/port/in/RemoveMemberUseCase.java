package com.baito.my_app.membership.application.port.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Owner kicks a part-timer out of the group. The membership is deactivated (kept for history),
 * which blocks further scheduling for that member.
 */
public interface RemoveMemberUseCase {

    void remove(Command command);

    @Getter
    @Setter
    @AllArgsConstructor
    class Command {
        private Long groupId;
        private Long ownerId;
        private Long memberId;
    }
}
