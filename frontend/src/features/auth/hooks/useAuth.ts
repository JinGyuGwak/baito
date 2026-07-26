import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'
import { authApi } from '../api'
import { authKeys } from '../keys'
import { useAuthStore } from '../store/auth.store'
import type { LoginResponse, MeResponse } from '../types'

/**
 * 로그인. 성공 시 토큰+회원정보를 authStore 에 저장합니다.
 * 서버 데이터가 아니라 세션 상태이므로 Query 캐시가 아닌 store 에 넣습니다.
 */
export function useLoginMutation() {
  const setAuth = useAuthStore((s) => s.setAuth)

  return useMutation<LoginResponse, ApiError, { loginId: string; password: string }>({
    mutationFn: authApi.login,
    onSuccess: ({ token, memberId, loginId, role }) => {
      setAuth({ token, user: { memberId, loginId, role } })
    },
  })
}

/** 로그아웃. 서버 토큰 무효화 후 로컬 세션/쿼리 캐시를 비웁니다. */
export function useLogoutMutation() {
  const clear = useAuthStore((s) => s.clear)
  const queryClient = useQueryClient()

  return useMutation<void, ApiError, void>({
    mutationFn: authApi.logout,
    // 서버 호출 성패와 무관하게 로컬 세션은 정리한다.
    onSettled: () => {
      clear()
      queryClient.clear()
    },
  })
}

/**
 * 현재 로그인한 회원 조회.
 * 토큰이 있을 때만 조회하며, 결과를 store.user 에도 동기화합니다.
 */
export function useCurrentUserQuery() {
  const token = useAuthStore((s) => s.token)

  return useQuery<MeResponse, ApiError>({
    queryKey: authKeys.me(),
    queryFn: authApi.me,
    enabled: Boolean(token),
    staleTime: 5 * 60_000,
  })
}
