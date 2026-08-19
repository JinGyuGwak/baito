package com.baito.my_app.invitation.domain;

import com.baito.my_app.common.exception.DomainException;

/**
 * Raised when a member tries to act on an invitation they are not party to
 * (e.g. cancelling someone else's invitation, or responding to an invitation not addressed to them).
 */
public class InvitationAccessDeniedException extends DomainException {
    public InvitationAccessDeniedException() {
        super("この招待に対する権限がありません。");
    }
}
