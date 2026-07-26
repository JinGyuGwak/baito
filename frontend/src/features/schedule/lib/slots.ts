/**
 * 타임라인 슬롯 모델.
 * 운영 시간 08:00~22:00 을 30분 단위 슬롯 28개로 표현한다.
 * 백엔드는 필요 인원을 30분 슬롯별 { startTime, requiredCount } 로 주고받으므로,
 * 화면 편집은 slot 배열(counts[28])로 하고 저장 시 연속 구간으로 합쳐서 보낸다.
 */
import type {
  AvailabilitySlot,
  RequiredStaffInterval,
  RequiredStaffSlot,
  TimeInterval,
} from '../types'

export const START_HOUR = 8
export const END_HOUR = 22
export const SLOT_MINUTES = 30
export const SLOT_COUNT = (END_HOUR - START_HOUR) * 2 // 28
export const SLOT_PX = 28 // .half-slot 높이와 일치

/** 슬롯 인덱스(0~28) → "HH:mm". 28은 마지막 경계(22:00). */
export function slotToTime(index: number): string {
  const totalMin = START_HOUR * 60 + index * SLOT_MINUTES
  const h = Math.floor(totalMin / 60)
  const m = totalMin % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
}

/** "HH:mm" 또는 "HH:mm:ss" → 슬롯 인덱스. 범위 밖이면 -1. */
export function timeToSlot(time: string): number {
  const [h, m] = time.split(':').map(Number)
  const index = ((h - START_HOUR) * 60 + m) / SLOT_MINUTES
  if (!Number.isInteger(index) || index < 0 || index >= SLOT_COUNT) return -1
  return index
}

/** GET 응답(슬롯별 인원) → counts 배열. */
export function slotsToCounts(slots: RequiredStaffSlot[]): number[] {
  const counts = new Array<number>(SLOT_COUNT).fill(0)
  for (const s of slots) {
    const i = timeToSlot(s.startTime)
    if (i >= 0) counts[i] = s.requiredCount
  }
  return counts
}

/** counts 배열 → 연속 동일값(0 제외) 구간 목록. PUT 요청 바디용. */
export function countsToIntervals(counts: number[]): RequiredStaffInterval[] {
  const intervals: RequiredStaffInterval[] = []
  let runStart = -1
  for (let i = 0; i <= SLOT_COUNT; i++) {
    const value = i < SLOT_COUNT ? counts[i] : 0
    const prev = runStart >= 0 ? counts[runStart] : 0
    if (runStart >= 0 && (value !== prev || i === SLOT_COUNT)) {
      intervals.push({
        startTime: slotToTime(runStart),
        endTime: slotToTime(i),
        requiredCount: prev,
      })
      runStart = -1
    }
    if (value > 0 && runStart < 0) runStart = i
  }
  return intervals
}

export interface DisplayBlock {
  startSlot: number
  endSlot: number
  count: number
}

/** counts → 화면 표시용 블록(연속 동일값 구간). */
export function countsToBlocks(counts: number[]): DisplayBlock[] {
  return countsToIntervals(counts).map((iv) => ({
    startSlot: timeToSlotBoundary(iv.startTime),
    endSlot: timeToSlotBoundary(iv.endTime),
    count: iv.requiredCount,
  }))
}

/** slotToTime 의 역 — 경계(28=22:00)까지 허용. */
function timeToSlotBoundary(time: string): number {
  const [h, m] = time.split(':').map(Number)
  return ((h - START_HOUR) * 60 + m) / SLOT_MINUTES
}

// ── 근무 가능 시간(가능/불가 이진) ──────────────────────────────

/** availability GET 응답(가능한 슬롯 목록) → boolean 배열. */
export function availabilityToBooleans(slots: AvailabilitySlot[]): boolean[] {
  const bools = new Array<boolean>(SLOT_COUNT).fill(false)
  for (const s of slots) {
    const i = timeToSlot(s.startTime)
    if (i >= 0) bools[i] = true
  }
  return bools
}

/** boolean 배열 → 연속 가능 구간 목록. PUT availability 요청 바디용. */
export function booleansToIntervals(bools: boolean[]): TimeInterval[] {
  const intervals: TimeInterval[] = []
  let runStart = -1
  for (let i = 0; i <= SLOT_COUNT; i++) {
    const on = i < SLOT_COUNT && bools[i]
    if (runStart >= 0 && !on) {
      intervals.push({ startTime: slotToTime(runStart), endTime: slotToTime(i) })
      runStart = -1
    }
    if (on && runStart < 0) runStart = i
  }
  return intervals
}

/** boolean 배열 → 화면 표시용 블록(연속 가능 구간). */
export function booleansToBlocks(bools: boolean[]): { startSlot: number; endSlot: number }[] {
  return booleansToIntervals(bools).map((iv) => ({
    startSlot: timeToSlotBoundary(iv.startTime),
    endSlot: timeToSlotBoundary(iv.endTime),
  }))
}
