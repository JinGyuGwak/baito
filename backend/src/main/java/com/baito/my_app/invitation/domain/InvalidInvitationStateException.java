package com.baito.my_app.invitation.domain;

import com.baito.my_app.common.exception.DomainException;

/**
 * Raised when a state transition (accept / reject / cancel) is attempted on an invitation that is
 * not in PENDING state.
 */
public class InvalidInvitationStateException extends DomainException {
    public InvalidInvitationStateException(InvitationStatus current) {
        super("PENDING 상태의 초대만 처리할 수 있습니다. 현재 상태: " + current);
    }
}
