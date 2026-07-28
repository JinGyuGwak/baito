import { apiClient } from '@/lib/api-client'
import type { MyProfile, SignUpRequest, SignUpResponse, UpdateNameRequest } from './types'

export const memberApi = {
  /** POST /api/members — 인증 불필요 */
  signUp: async (body: SignUpRequest): Promise<SignUpResponse> => {
    const { data } = await apiClient.post<SignUpResponse>('/members', body)
    return data
  },

  /** GET /api/members/me — 내 프로필 조회 */
  getMyProfile: async (): Promise<MyProfile> => {
    const { data } = await apiClient.get<MyProfile>('/members/me')
    return data
  },

  /** PATCH /api/members/me — 이름 변경 */
  updateName: async (body: UpdateNameRequest): Promise<MyProfile> => {
    const { data } = await apiClient.patch<MyProfile>('/members/me', body)
    return data
  },
}
