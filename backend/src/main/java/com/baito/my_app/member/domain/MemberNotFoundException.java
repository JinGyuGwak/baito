package com.baito.my_app.member.domain;

import com.baito.my_app.common.exception.DomainException;

public class MemberNotFoundException extends DomainException {
    public MemberNotFoundException(String message) {
        super(message);
    }
}
