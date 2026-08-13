package com.baito.my_app.group.domain;

import com.baito.my_app.common.exception.DomainException;

public class GroupNotFoundException extends DomainException {
    public GroupNotFoundException(Long groupId) {
        super("存在しないグループです: " + groupId);
    }
}
