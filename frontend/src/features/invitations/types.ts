/** 초대 상태 */
export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED'

/** GET /api/invitations/received — 응답 요소 (알바가 받은 초대) */
export interface Invitation {
  id: number
  groupId: number
  /** 그룹명 (조회 실패 시 null) */
  groupName: string | null
  /** 초대한 사장 회원 ID */
  inviterId: number
  /** 초대한 사장 이름 (조회 실패 시 null) */
  inviterName: string | null
  /** 초대받은 알바 회원 ID */
  inviteeId: number
  status: InvitationStatus
  /** ISO-8601 */
  createdAt: string
  /** 미응답 시 null */
  respondedAt: string | null
}

/** GET /api/invitations/sent — 응답 요소 (사장이 보낸 초대, 알바 이름 포함) */
export interface SentInvitation {
  id: number
  groupId: number
  /** 초대받은 알바 회원 ID */
  inviteeId: number
  /** 초대받은 알바 이름 (조회 실패 시 null) */
  inviteeName: string | null
  /** 초대받은 알바 로그인 ID (조회 실패 시 null) */
  inviteeLoginId: string | null
  status: InvitationStatus
  createdAt: string
  respondedAt: string | null
}

/** GET /api/invitations/sent — 페이지 응답 */
export interface SentInvitationPage {
  content: SentInvitation[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

/** 보낸 초대 조회 파라미터 */
export interface SentInvitationParams {
  groupId: number
  /** 생략 시 전체 상태 */
  status?: InvitationStatus
  page?: number
  size?: number
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
