import { QueryClient } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'

/**
 * 앱 전역 QueryClient. 기본 옵션은 이 스케줄 앱 특성에 맞춘 값입니다.
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000, // 30초: 과도한 refetch 방지
      refetchOnWindowFocus: false,
      retry: (failureCount, error) => {
        // 4xx(클라이언트 잘못)는 재시도 무의미. 네트워크/5xx 만 1회 재시도.
        const code = (error as unknown as ApiError)?.code
        if (code === 'NETWORK_ERROR') return failureCount < 1
        return false
      },
    },
    mutations: {
      retry: false,
    },
  },
})
