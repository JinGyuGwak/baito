import { useMutation } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'
import { memberApi } from '../api'
import type { SignUpRequest, SignUpResponse } from '../types'

/** 회원가입. 인증 불필요하며 조회 캐시가 없어 무효화 대상도 없습니다. */
export function useSignUpMutation() {
  return useMutation<SignUpResponse, ApiError, SignUpRequest>({
    mutationFn: memberApi.signUp,
  })
}
