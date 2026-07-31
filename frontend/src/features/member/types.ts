import type { Role } from '@/types/api'

/** POST /api/members — 요청 */
export interface SignUpRequest {
  loginId: string
  password: string
  name: string
  role: Role
}

/** POST /api/members — 응답 */
export interface SignUpResponse {
  memberId: number
}

/** GET /api/members/me — 응답 (내 프로필) */
export interface MyProfile {
  memberId: number
  loginId: string
  name: string
  role: Role
}

/** PATCH /api/members/me — 요청 (이름 변경) */
export interface UpdateNameRequest {
  name: string
}
