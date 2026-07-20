package com.baito.my_app.invitation.domain;

import com.baito.my_app.common.exception.DomainException;

/**
 * Raised when the invitee cannot be invited: no such login ID, the account is not a PART_TIMER,
 * or the member is already an active member of the group.
 */
public class InvalidInviteeException extends DomainException {
    public InvalidInviteeException(String message) {
        super(message);
    }
}
