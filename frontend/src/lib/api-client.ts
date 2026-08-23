import axios, { AxiosError } from 'axios'
import { env } from '@/config/env'
import type { ApiError } from '@/types/api'
import { getAuthToken, useAuthStore } from '@/features/auth/store/auth.store'

/**
 * 단일 axios 인스턴스. 모든 API 호출은 이 인스턴스를 거칩니다.
 * 컴포넌트는 이 파일을 직접 import 하지 말고 각 feature 의 api.ts → hooks 를 경유하세요.
 */
export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: { 'Content-Type': 'application/json' },
})

/** Request: 스토어의 opaque 토큰을 Authorization 헤더에 주입. */
apiClient.interceptors.request.use((config) => {
  const token = getAuthToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * Response: 에러를 프로젝트 공용 ApiError 로 정규화하고 401 을 처리합니다.
 *
 * 이 백엔드는 JWT 가 아니라 Redis 서버측 세션 기반의 opaque 토큰이라
 * "리프레시" 개념이 없습니다. 따라서 401 은 곧 재로그인 신호 → 세션을 비웁니다.
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().clear()
      // 라우터 밖이므로 하드 리다이렉트. 이미 로그인 페이지면 생략.
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
    }

    const normalized: ApiError = error.response?.data ?? {
      code: 'NETWORK_ERROR',
      message: error.message || 'ネットワークエラーが発生しました。',
    }
    return Promise.reject(normalized)
  },
)
