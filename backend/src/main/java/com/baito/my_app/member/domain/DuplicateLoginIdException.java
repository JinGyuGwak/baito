package com.baito.my_app.member.domain;

import com.baito.my_app.common.exception.DomainException;

public class DuplicateLoginIdException extends DomainException {
    public DuplicateLoginIdException(String loginId) {
        super("すでに使用されているログインIDです: " + loginId);
    }
}
