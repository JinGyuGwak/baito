import type { Role } from '@/types/api'

/** POST /api/auth/login — 요청 */
export interface LoginRequest {
  loginId: string
  password: string
}

/** POST /api/auth/login — 응답 */
export interface LoginResponse {
  token: string
  memberId: number
  loginId: string
  role: Role
}

/** GET /api/auth/me — 응답 */
export interface MeResponse {
  memberId: number
  loginId: string
  role: Role
}
