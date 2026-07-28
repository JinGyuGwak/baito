package com.baito.my_app.member.application.port.out;

import com.baito.my_app.member.domain.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port for member persistence. Implemented by a JPA adapter in the infrastructure layer.
 */
public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findById(Long id);

    List<Member> findAllByIds(Collection<Long> ids);

    Optional<Member> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
