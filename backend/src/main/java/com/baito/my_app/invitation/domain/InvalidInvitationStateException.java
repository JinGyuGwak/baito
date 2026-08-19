package com.baito.my_app.invitation.domain;

import com.baito.my_app.common.exception.DomainException;

/**
 * Raised when a state transition (accept / reject / cancel) is attempted on an invitation that is
 * not in PENDING state.
 */
public class InvalidInvitationStateException extends DomainException {
    public InvalidInvitationStateException(InvitationStatus current) {
        super("PENDING状態の招待のみ処理できます。現在の状態: " + current);
    }
}
