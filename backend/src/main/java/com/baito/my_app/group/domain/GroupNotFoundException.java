package com.baito.my_app.group.domain;

import com.baito.my_app.common.exception.DomainException;

public class GroupNotFoundException extends DomainException {
    public GroupNotFoundException(Long groupId) {
        super("존재하지 않는 그룹입니다: " + groupId);
    }
}
