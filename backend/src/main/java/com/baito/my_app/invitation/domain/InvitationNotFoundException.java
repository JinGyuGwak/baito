package com.baito.my_app.invitation.domain;

import com.baito.my_app.common.exception.DomainException;

public class InvitationNotFoundException extends DomainException {
    public InvitationNotFoundException(Long invitationId) {
        super("존재하지 않는 초대입니다: " + invitationId);
    }
}
