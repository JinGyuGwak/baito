package com.baito.my_app.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    @DisplayName("register 팩토리는 이미 해시된 비밀번호를 담고 id는 비어 있다")
    void register() {
        Member member = Member.register("owner01", "hashed", "홍길동", Role.OWNER);

        assertThat(member.getId()).isNull();
        assertThat(member.getLoginId()).isEqualTo("owner01");
        assertThat(member.getPassword()).isEqualTo("hashed");
    }

    @Test
    @DisplayName("역할 판별: OWNER")
    void ownerRole() {
        Member owner = Member.register("owner01", "hashed", "사장", Role.OWNER);

        assertThat(owner.isOwner()).isTrue();
        assertThat(owner.isPartTimer()).isFalse();
    }

    @Test
    @DisplayName("역할 판별: PART_TIMER")
    void partTimerRole() {
        Member worker = Member.register("worker01", "hashed", "알바", Role.PART_TIMER);

        assertThat(worker.isPartTimer()).isTrue();
        assertThat(worker.isOwner()).isFalse();
    }
}
