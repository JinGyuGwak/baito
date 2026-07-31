package com.baito.my_app.member.adapter.out.persistence;

import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class MemberPersistenceAdapter implements MemberRepository {

    private final MemberJpaRepository jpaRepository;

    public MemberPersistenceAdapter(MemberJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Member save(Member member) {
        MemberJpaEntity saved = jpaRepository.save(toEntity(member));
        return toDomain(saved);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return jpaRepository.findById(id).map(MemberPersistenceAdapter::toDomain);
    }

    @Override
    public List<Member> findAllByIds(Collection<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(MemberPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        return jpaRepository.findByLoginId(loginId).map(MemberPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        return jpaRepository.existsByLoginId(loginId);
    }

    private static MemberJpaEntity toEntity(Member member) {
        return new MemberJpaEntity(
                member.getId(),
                member.getLoginId(),
                member.getPassword(),
                member.getName(),
                member.getRole(),
                member.getCreatedAt()
        );
    }

    static Member toDomain(MemberJpaEntity entity) {
        return new Member(
                entity.getId(),
                entity.getLoginId(),
                entity.getPassword(),
                entity.getName(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }
}
