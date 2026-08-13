package com.baito.my_app.invitation.domain;

import com.baito.my_app.common.exception.DomainException;

public class DuplicatePendingInvitationException extends DomainException {
    public DuplicatePendingInvitationException() {
        super("すでに保留中(PENDING)の招待が存在します。");
    }
}
