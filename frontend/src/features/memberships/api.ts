import { apiClient } from '@/lib/api-client'
import type { MembershipGroup } from './types'

export const membershipApi = {
  /** GET /api/memberships/groups — 내가 가입한 그룹 목록 (PART_TIMER) */
  listGroups: async (): Promise<MembershipGroup[]> => {
    const { data } = await apiClient.get<MembershipGroup[]>('/memberships/groups')
    return data
  },
}
