/** 필요 인원 대비 배정 충원 현황 계산. 대시보드/배정 화면에서 공유. */
import type { AssignmentSlot } from '../types'
import type { RequiredStaffSlot } from '@/features/schedule'
import { SLOT_COUNT, slotsToCounts, timeToSlot } from '@/features/schedule/lib/slots'

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
