/** POST /api/groups/{groupId}/assignments — 요청 (OWNER) */
export interface CreateAssignmentRequest {
  /** 배정할 알바 회원 ID */
  memberId: number
  /** yyyy-MM-dd */
  workDate: string
  /** HH:mm, 30분 단위 */
  startTime: string
  /** HH:mm, 30분 단위 */
  endTime: string
}

/** POST /api/groups/{groupId}/assignments — 응답 */
export interface CreateAssignmentResponse {
  /** 새로 배정된 30분 슬롯 수 */
  assignedSlotCount: number
}

/** POST /api/groups/{groupId}/assignments/cancel — 요청 (OWNER). 범위 내 확정 슬롯을 취소 */
export interface CancelAssignmentRequest {
  memberId: number
  /** yyyy-MM-dd */
  workDate: string
  /** HH:mm, 30분 단위 */
  startTime: string
  /** HH:mm, 30분 단위 */
  endTime: string
}

/** POST /api/groups/{groupId}/assignments/cancel — 응답 */
export interface CancelAssignmentResponse {
  /** 취소된 30분 슬롯 수 */
  cancelledSlotCount: number
}

/** GET /api/groups/{groupId}/assignments — 응답 요소 (30분 슬롯) */
export interface AssignmentSlot {
  /** 배정된 알바 회원 ID */
  memberId: number
  /** 배정된 알바 이름 (조회 실패 시 null) */
  memberName: string | null
  /** 배정된 알바 로그인 ID (조회 실패 시 null) */
  memberLoginId: string | null
  /** HH:mm:ss */
  startTime: string
}

/** GET /api/groups/{groupId}/assignments/candidates — 응답 요소 */
export interface AssignmentCandidate {
  memberId: number
  name: string
  loginId: string
  /** 해당 날짜에 이 알바가 근무 가능한 시간대 목록 */
  availableIntervals: { startTime: string; endTime: string }[]
  /** 선택한 시간대 전체에 이미 배정됨 → 선택 불가 */
  alreadyAssigned: boolean
}

/** GET /api/me/assignments — 응답 요소 (알바 본인 확정 근무, 30분 슬롯) */
export interface MyAssignmentSlot {
  groupId: number
  /** HH:mm:ss */
  startTime: string
}
