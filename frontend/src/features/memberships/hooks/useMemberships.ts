import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'
import { membershipApi } from '../api'
import { membershipKeys } from '../keys'
import type { GroupMember, MembershipGroup } from '../types'

/** GET /api/memberships/groups — 내가 가입한 그룹 목록 (PART_TIMER 전용 엔드포인트) */
export function useMyGroupsQuery(options?: { enabled?: boolean }) {
  return useQuery<MembershipGroup[], ApiError>({
    queryKey: membershipKeys.groups(),
    queryFn: membershipApi.listGroups,
    enabled: options?.enabled ?? true,
  })
}

/** GET /api/groups/{groupId}/members — 그룹 소속 알바생 목록 (OWNER) */
export function useGroupMembersQuery(groupId: number, options?: { enabled?: boolean }) {
  return useQuery<GroupMember[], ApiError>({
    queryKey: membershipKeys.groupMembers(groupId),
    queryFn: () => membershipApi.listGroupMembers(groupId),
    enabled: (options?.enabled ?? true) && Boolean(groupId),
  })
}

/** DELETE /api/groups/{groupId}/members/{memberId} — 알바생 추방 (OWNER) */
export function useRemoveGroupMemberMutation(groupId: number) {
  const queryClient = useQueryClient()

  return useMutation<void, ApiError, number>({
    mutationFn: (memberId) => membershipApi.removeGroupMember(groupId, memberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: membershipKeys.groupMembers(groupId) })
    },
  })
}
