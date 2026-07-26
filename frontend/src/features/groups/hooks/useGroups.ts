import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'
import { groupApi } from '../api'
import { groupKeys } from '../keys'
import type { CreateGroupRequest, CreateGroupResponse, Group } from '../types'

/** GET /api/groups — 내 그룹 목록 (OWNER 전용 엔드포인트) */
export function useGroupsQuery(options?: { enabled?: boolean }) {
  return useQuery<Group[], ApiError>({
    queryKey: groupKeys.lists(),
    queryFn: groupApi.list,
    enabled: options?.enabled ?? true,
  })
}

/** POST /api/groups — 생성 성공 시 목록 무효화 */
export function useCreateGroupMutation() {
  const queryClient = useQueryClient()

  return useMutation<CreateGroupResponse, ApiError, CreateGroupRequest>({
    mutationFn: groupApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: groupKeys.lists() })
    },
  })
}
