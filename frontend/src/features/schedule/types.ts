/** 시간 구간 (HH:mm). 요청 시 사용. */
export interface TimeInterval {
  startTime: string
  endTime: string
}

// ── 근무 가능 시간 (Availability) ────────────────────────────────

/** PUT /api/groups/{groupId}/availability — 요청 (해당 날짜 전체 교체) */
export interface SetAvailabilityRequest {
  /** yyyy-MM-dd */
  workDate: string
  intervals: TimeInterval[]
}

/** GET /api/groups/{groupId}/availability — 응답 요소 (30분 슬롯) */
export interface AvailabilitySlot {
  /** HH:mm:ss */
  startTime: string
}

// ── 필요 인원 (Required Staff) ───────────────────────────────────

/** 필요 인원 구간 (HH:mm + 인원수) */
export interface RequiredStaffInterval extends TimeInterval {
  /** 0 이상 */
  requiredCount: number
}

/** PUT /api/groups/{groupId}/required-staff — 요청 (해당 날짜 전체 교체) */
export interface SetRequiredStaffRequest {
  /** yyyy-MM-dd */
  workDate: string
  intervals: RequiredStaffInterval[]
}

/** GET /api/groups/{groupId}/required-staff — 응답 요소 (30분 슬롯) */
export interface RequiredStaffSlot {
  /** HH:mm:ss */
  startTime: string
  requiredCount: number
}
