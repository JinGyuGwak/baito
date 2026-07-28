package com.baito.my_app.member.adapter.out.persistence;

import com.baito.my_app.member.application.port.out.MemberRepository;
import com.baito.my_app.member.domain.Member;
import com.baito.my_app.member.domain.Role;
import com.baito.my_app.support.PersistenceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(MemberPersistenceAdapter.class)
class MemberPersistenceAdapterTest extends PersistenceTestSupport {

    @Autowired
    private MemberRepository memberRepository; // 실제 구현체 = MemberPersistenceAdapter

    @Autowired
    private MemberJpaRepository jpaRepository;

    @Test
    @DisplayName("저장하면 id와 createdAt이 채번/기록되고 loginId로 다시 조회된다")
    void save_and_find() {
        Member saved = memberRepository.save(Member.register("owner01", "hashed-pw", "홍길동", Role.OWNER));

        assertThat(saved.getId()).isNotNull(); // IDENTITY 채번 확인
        assertThat(saved.getCreatedAt()).isNotNull(); // @CreationTimestamp 확인

        assertThat(memberRepository.findById(saved.getId())).isPresent();
        assertThat(memberRepository.findByLoginId("owner01")).isPresent()
                .get()
                .satisfies(m -> {
                    assertThat(m.getPassword()).isEqualTo("hashed-pw");
                    assertThat(m.getRole()).isEqualTo(Role.OWNER); // enum → VARCHAR 매핑 확인
                });
    }

    @Test
    @DisplayName("existsByLoginId - 존재 여부를 실제 쿼리로 판별한다")
    void existsByLoginId() {
        memberRepository.save(Member.register("owner01", "hashed-pw", "홍길동", Role.OWNER));

        assertThat(memberRepository.existsByLoginId("owner01")).isTrue();
        assertThat(memberRepository.existsByLoginId("nobody")).isFalse();
    }

    @Test
    @DisplayName("loginId UNIQUE 제약 - 같은 loginId를 두 번 저장하면 DB 제약 위반")
    void duplicateLoginId_violatesUniqueConstraint() {
        memberRepository.save(Member.register("owner01", "pw1", "사람1", Role.OWNER));

        // saveAndFlush로 즉시 flush해야 UNIQUE 인덱스 위반이 드러난다.
        assertThatThrownBy(() -> jpaRepository.saveAndFlush(
                new MemberJpaEntity(null, "owner01", "pw2", "사람2", Role.PART_TIMER, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
