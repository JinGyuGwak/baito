package com.baito.my_app.common.domain;

import com.baito.my_app.common.exception.InvalidSlotTimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlotTimesTest {

    @Nested
    @DisplayName("isAligned")
    class IsAligned {

        @Test
        @DisplayName("정각/30분 경계이고 초·나노가 0이면 true")
        void aligned() {
            assertThat(SlotTimes.isAligned(LocalTime.of(9, 0))).isTrue();
            assertThat(SlotTimes.isAligned(LocalTime.of(9, 30))).isTrue();
        }

        @Test
        @DisplayName("30분 경계가 아니거나 초가 있으면 false, null도 false")
        void notAligned() {
            assertThat(SlotTimes.isAligned(LocalTime.of(9, 15))).isFalse();
            assertThat(SlotTimes.isAligned(LocalTime.of(9, 0, 30))).isFalse();
            assertThat(SlotTimes.isAligned(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("expand")
    class Expand {

        @Test
        @DisplayName("반열린 구간 [start, end)를 30분 슬롯 시작 시각 목록으로 전개한다")
        void expandsInterval() {
            assertThat(SlotTimes.expand(LocalTime.of(15, 0), LocalTime.of(17, 0)))
                    .containsExactly(
                            LocalTime.of(15, 0),
                            LocalTime.of(15, 30),
                            LocalTime.of(16, 0),
                            LocalTime.of(16, 30));
        }

        @Test
        @DisplayName("한 슬롯 길이(30분)면 시작 시각 하나만 반환한다")
        void singleSlot() {
            assertThat(SlotTimes.expand(LocalTime.of(9, 0), LocalTime.of(9, 30)))
                    .containsExactly(LocalTime.of(9, 0));
        }

        @Test
        @DisplayName("경계가 30분 단위가 아니면 InvalidSlotTimeException")
        void misaligned() {
            assertThatThrownBy(() -> SlotTimes.expand(LocalTime.of(9, 10), LocalTime.of(10, 0)))
                    .isInstanceOf(InvalidSlotTimeException.class);
        }

        @Test
        @DisplayName("종료가 시작보다 뒤가 아니면 InvalidSlotTimeException")
        void endNotAfterStart() {
            assertThatThrownBy(() -> SlotTimes.expand(LocalTime.of(10, 0), LocalTime.of(10, 0)))
                    .isInstanceOf(InvalidSlotTimeException.class);
            assertThatThrownBy(() -> SlotTimes.expand(LocalTime.of(10, 30), LocalTime.of(10, 0)))
                    .isInstanceOf(InvalidSlotTimeException.class);
        }
    }
}
