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

/** GET /api/groups/{groupId}/assignments — 응답 요소 (30분 슬롯) */
export interface AssignmentSlot {
  /** 배정된 알바 회원 ID */
  memberId: number
  /** HH:mm:ss */
  startTime: string
}

/** GET /api/me/assignments — 응답 요소 (알바 본인 확정 근무, 30분 슬롯) */
export interface MyAssignmentSlot {
  groupId: number
  /** HH:mm:ss */
  startTime: string
}
