import { apiClient } from '@/lib/api-client'
import type { LoginRequest, LoginResponse, MeResponse } from './types'

/** 순수 API 호출 함수 모음. Promise 만 반환하고 캐싱/상태는 hooks 가 담당. */
export const authApi = {
  /** POST /api/auth/login */
  login: async (body: LoginRequest): Promise<LoginResponse> => {
    const { data } = await apiClient.post<LoginResponse>('/auth/login', body)
    return data
  },

  /** POST /api/auth/logout — 204 No Content */
  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout')
  },

  /** GET /api/auth/me */
  me: async (): Promise<MeResponse> => {
    const { data } = await apiClient.get<MeResponse>('/auth/me')
    return data
  },
}
