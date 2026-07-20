package com.baito.my_app.invitation.domain;

import com.baito.my_app.common.exception.DomainException;

public class DuplicatePendingInvitationException extends DomainException {
    public DuplicatePendingInvitationException() {
        super("이미 대기 중(PENDING)인 초대가 존재합니다.");
    }
}
