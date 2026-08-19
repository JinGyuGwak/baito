package com.baito.my_app.invitation.domain;

import com.baito.my_app.common.exception.DomainException;

public class InvitationNotFoundException extends DomainException {
    public InvitationNotFoundException(Long invitationId) {
        super("存在しない招待です: " + invitationId);
    }
}
