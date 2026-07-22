package com.baito.my_app.group.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkGroupTest {

    @Test
    @DisplayName("create 팩토리는 소유자와 이름을 담고 id/createdAt은 비어 있다")
    void create() {
        WorkGroup group = WorkGroup.create(1L, "강남점", "설명");

        assertThat(group.getId()).isNull();
        assertThat(group.getCreatedAt()).isNull();
        assertThat(group.getOwnerId()).isEqualTo(1L);
        assertThat(group.getName()).isEqualTo("강남점");
    }

    @Test
    @DisplayName("isOwnedBy는 소유자 id일 때만 true")
    void isOwnedBy() {
        WorkGroup group = WorkGroup.create(1L, "강남점", null);

        assertThat(group.isOwnedBy(1L)).isTrue();
        assertThat(group.isOwnedBy(2L)).isFalse();
    }
}
