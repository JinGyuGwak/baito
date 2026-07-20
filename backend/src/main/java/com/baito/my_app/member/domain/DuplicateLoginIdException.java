package com.baito.my_app.member.domain;

import com.baito.my_app.common.exception.DomainException;

public class DuplicateLoginIdException extends DomainException {
    public DuplicateLoginIdException(String loginId) {
        super("이미 사용 중인 로그인 ID입니다: " + loginId);
    }
}
