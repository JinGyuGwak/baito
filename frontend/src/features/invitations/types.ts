/** 초대 상태 */
export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED'

/** GET /api/invitations/{sent,received} — 응답 요소 */
export interface Invitation {
  id: number
  groupId: number
  /** 초대한 사장 회원 ID */
  inviterId: number
  /** 초대받은 알바 회원 ID */
  inviteeId: number
  status: InvitationStatus
  /** ISO-8601 */
  createdAt: string
  /** 미응답 시 null */
  respondedAt: string | null
}

/** POST /api/invitations — 요청 (OWNER) */
export interface CreateInvitationRequest {
  groupId: number
  /** 초대받는 알바의 로그인 ID */
  inviteeLoginId: string
}

/** POST /api/invitations — 응답 */
export interface CreateInvitationResponse {
  invitationId: number
}
