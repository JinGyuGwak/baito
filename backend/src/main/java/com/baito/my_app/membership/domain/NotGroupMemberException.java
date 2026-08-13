package com.baito.my_app.membership.domain;

import com.baito.my_app.common.exception.DomainException;

/**
 * Raised when a part-timer acts on a group they are not an active member of.
 */
public class NotGroupMemberException extends DomainException {
    public NotGroupMemberException(Long groupId) {
        super("このグループの有効なメンバーではありません: " + groupId);
    }
}
