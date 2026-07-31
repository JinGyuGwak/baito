package com.baito.my_app.member.application.port.in;

import com.baito.my_app.member.domain.Member;

/**
 * A logged-in member reads their own profile and changes their display name.
 * loginId and role are fixed at sign-up and cannot be changed here.
 */
public interface MemberProfileUseCase {

    Member getMyProfile(Long memberId);

    /**
     * @return the member with the updated name
     */
    Member changeName(Long memberId, String name);
}
