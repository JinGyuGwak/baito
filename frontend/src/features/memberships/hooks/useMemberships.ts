import { useQuery } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'
import { membershipApi } from '../api'
import { membershipKeys } from '../keys'
import type { MembershipGroup } from '../types'

/** GET /api/memberships/groups — 내가 가입한 그룹 목록 (PART_TIMER 전용 엔드포인트) */
export function useMyGroupsQuery(options?: { enabled?: boolean }) {
  return useQuery<MembershipGroup[], ApiError>({
    queryKey: membershipKeys.groups(),
    queryFn: membershipApi.listGroups,
    enabled: options?.enabled ?? true,
  })
}
