import { apiClient } from '@/lib/api-client'
import type { SignUpRequest, SignUpResponse } from './types'

export const memberApi = {
  /** POST /api/members — 인증 불필요 */
  signUp: async (body: SignUpRequest): Promise<SignUpResponse> => {
    const { data } = await apiClient.post<SignUpResponse>('/members', body)
    return data
  },
}
