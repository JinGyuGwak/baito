import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'
import { useAuthStore } from '@/features/auth'
import { memberApi } from '../api'
import { memberKeys } from '../keys'
import type { MyProfile, SignUpRequest, SignUpResponse, UpdateNameRequest } from '../types'

/** 회원가입. 인증 불필요하며 조회 캐시가 없어 무효화 대상도 없습니다. */
export function useSignUpMutation() {
  return useMutation<SignUpResponse, ApiError, SignUpRequest>({
    mutationFn: memberApi.signUp,
  })
}

/** GET /api/members/me — 내 프로필 조회 */
export function useMyProfileQuery(options?: { enabled?: boolean }) {
  return useQuery<MyProfile, ApiError>({
    queryKey: memberKeys.me(),
    queryFn: memberApi.getMyProfile,
    enabled: options?.enabled ?? true,
  })
}

/**
 * PATCH /api/members/me — 이름 변경.
 * 성공 시 프로필 캐시를 갱신하고, AppBar 등이 참조하는 auth 스토어의 name 도 동기화합니다.
 */
export function useUpdateNameMutation() {
  const queryClient = useQueryClient()
  const setName = useAuthStore((s) => s.setName)

  return useMutation<MyProfile, ApiError, UpdateNameRequest>({
    mutationFn: memberApi.updateName,
    onSuccess: (profile) => {
      queryClient.setQueryData(memberKeys.me(), profile)
      setName(profile.name)
    },
  })
}
