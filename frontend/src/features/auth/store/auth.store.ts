import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Role } from '@/types/api'

/** 로그인한 회원의 최소 식별 정보 (login/me 응답 공통 부분). */
export interface AuthUser {
  memberId: number
  loginId: string
  role: Role
}

interface AuthState {
  /** opaque bearer 토큰. 없으면 미인증. */
  token: string | null
  user: AuthUser | null
  setAuth: (payload: { token: string; user: AuthUser }) => void
  setUser: (user: AuthUser) => void
  clear: () => void
}

/**
 * 인증 상태(토큰 + 회원 정보) 전용 스토어.
 *
 * - 서버 데이터가 아니라 "클라이언트가 들고 있어야 하는 세션 상태"이므로 zustand 담당.
 * - localStorage 에 persist 하여 새로고침에도 로그인 유지.
 * - 토큰 접근은 반드시 이 스토어를 경유(api-client 인터셉터도 여기만 바라봄).
 *   → 추후 httpOnly 쿠키 방식으로 옮길 때 이 파일만 바꾸면 됩니다.
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      setAuth: ({ token, user }) => set({ token, user }),
      setUser: (user) => set({ user }),
      clear: () => set({ token: null, user: null }),
    }),
    { name: 'baito-auth' },
  ),
)

/** React 훅 밖(인터셉터 등)에서 토큰을 읽을 때 사용. */
export const getAuthToken = () => useAuthStore.getState().token
