import { apiClient } from '@/lib/api-client'
import type { GroupMember, MembershipGroup } from './types'

export const membershipApi = {
  /** GET /api/memberships/groups — 내가 가입한 그룹 목록 (PART_TIMER) */
  listGroups: async (): Promise<MembershipGroup[]> => {
    const { data } = await apiClient.get<MembershipGroup[]>('/memberships/groups')
    return data
  },

  /** GET /api/groups/{groupId}/members — 그룹 소속 알바생 목록 (OWNER) */
  listGroupMembers: async (groupId: number): Promise<GroupMember[]> => {
    const { data } = await apiClient.get<GroupMember[]>(`/groups/${groupId}/members`)
    return data
  },

  /** DELETE /api/groups/{groupId}/members/{memberId} — 알바생 추방 (OWNER), 204 */
  removeGroupMember: async (groupId: number, memberId: number): Promise<void> => {
    await apiClient.delete(`/groups/${groupId}/members/${memberId}`)
  },
}
