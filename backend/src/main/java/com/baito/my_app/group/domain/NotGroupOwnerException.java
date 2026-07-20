package com.baito.my_app.group.domain;

import com.baito.my_app.common.exception.DomainException;

/**
 * Raised when a member tries to perform an owner-only action on a group they do not own.
 */
public class NotGroupOwnerException extends DomainException {
    public NotGroupOwnerException(Long groupId) {
        super("해당 그룹의 소유자가 아닙니다: " + groupId);
    }
}
