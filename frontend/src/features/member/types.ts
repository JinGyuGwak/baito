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
