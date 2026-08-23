/** 필요 인원 대비 배정 충원 현황 계산. 대시보드/배정/스케줄 화면에서 공유. */
import type { AssignmentSlot } from '../types'
import type { RequiredStaffSlot } from '@/features/schedule'
import type { TimelineBlock } from '@/features/schedule/components/ScheduleTimeline'
import { SLOT_COUNT, countsToBlocks, slotToTime, slotsToCounts, timeToSlot } from '@/features/schedule/lib/slots'

export interface Coverage {
  /** 하루 필요 인원 슬롯 합(필요 인원 × 슬롯 수). */
  totalNeeded: number
  /** 요구 대비 채워진 합(슬롯별 min(배정, 필요)). */
  totalFilled: number
  /** 배정이 필요 인원에 못 미치는 슬롯 수. */
  shortSlots: number
}

export function computeCoverage(required: RequiredStaffSlot[], assignments: AssignmentSlot[]): Coverage {
  const counts = slotsToCounts(required)
  const assigned = new Array<number>(SLOT_COUNT).fill(0)
  for (const a of assignments) {
    const i = timeToSlot(a.startTime)
    if (i >= 0) assigned[i]++
  }

  let totalNeeded = 0
  let totalFilled = 0
  let shortSlots = 0
  for (let i = 0; i < SLOT_COUNT; i++) {
    totalNeeded += counts[i]
    totalFilled += Math.min(assigned[i], counts[i])
    if (assigned[i] < counts[i]) shortSlots++
  }
  return { totalNeeded, totalFilled, shortSlots }
}

export interface CoverageBlocks {
  blocks: TimelineBlock[]
  totalNeeded: number
  totalFilled: number
  /** 배정이 필요 인원에 못 미치는 블록 수. */
  shortBlocks: number
}

/**
 * 슬롯별 필요 인원/배정 회원을 타임라인 블록 + 요약 통계로 변환한다.
 * 배정 탭과 스케줄(필요 인원) 탭이 같은 배정 현황 표시를 공유한다.
 */
export function buildCoverageBlocks(
  requiredCounts: number[],
  assignedBySlot: { memberId: number; label: string }[][],
): CoverageBlocks {
  const blocks: TimelineBlock[] = []
  let shortBlocks = 0
  let totalNeeded = 0
  let totalFilled = 0

  for (let i = 0; i < SLOT_COUNT; i++) {
    totalNeeded += requiredCounts[i]
    totalFilled += Math.min(assignedBySlot[i].length, requiredCounts[i])
  }

  const unionOf = (start: number, end: number) => {
    const union = new Map<number, string>()
    for (let i = start; i < end; i++) {
      for (const m of assignedBySlot[i]) union.set(m.memberId, m.label)
    }
    return union
  }

  // 1) 필요 인원 블록 — 배정 커버리지로 색칠
  for (const b of countsToBlocks(requiredCounts)) {
    let maxShort = 0
    for (let i = b.startSlot; i < b.endSlot; i++) {
      maxShort = Math.max(maxShort, Math.max(0, b.count - assignedBySlot[i].length))
    }
    const union = unionOf(b.startSlot, b.endSlot)
    const short = maxShort > 0
    if (short) shortBlocks++
    blocks.push({
      key: `req-${b.startSlot}`,
      startSlot: b.startSlot,
      endSlot: b.endSlot,
      tone: short ? 'warn' : 'success',
      title: `${b.count}名必要`,
      sub: `${slotToTime(b.startSlot)} – ${slotToTime(b.endSlot)} · 割り当て ${union.size}${short ? ` · ${maxShort}名不足` : ' · 充足済み'}`,
      avatars: [...union].map(([id, label]) => ({ id, label })),
    })
  }

  // 2) 필요 인원 미설정인데 배정된 슬롯 — 별도 표시(연속 구간으로 합침)
  let runStart = -1
  for (let i = 0; i <= SLOT_COUNT; i++) {
    const extra = i < SLOT_COUNT && requiredCounts[i] === 0 && assignedBySlot[i].length > 0
    if (runStart >= 0 && !extra) {
      const union = unionOf(runStart, i)
      blocks.push({
        key: `extra-${runStart}`,
        startSlot: runStart,
        endSlot: i,
        tone: 'muted',
        title: `割り当て ${union.size}名`,
        sub: `${slotToTime(runStart)} – ${slotToTime(i)} · 必要人数未設定`,
        avatars: [...union].map(([id, label]) => ({ id, label })),
      })
      runStart = -1
    }
    if (extra && runStart < 0) runStart = i
  }

  return { blocks, totalNeeded, totalFilled, shortBlocks }
}
