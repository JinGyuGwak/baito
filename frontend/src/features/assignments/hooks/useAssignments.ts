import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'
import { assignmentApi } from '../api'
import { assignmentKeys } from '../keys'
import type {
  AssignmentSlot,
  CreateAssignmentRequest,
  CreateAssignmentResponse,
  MyAssignmentSlot,
} from '../types'

/** GET /api/groups/{groupId}/assignments?date=... */
export function useAssignmentsQuery(groupId: number, date: string) {
  return useQuery<AssignmentSlot[], ApiError>({
    queryKey: assignmentKeys.byDate(groupId, date),
    queryFn: () => assignmentApi.list(groupId, date),
    enabled: Boolean(groupId && date),
  })
}

/** GET /api/me/assignments?date=... — 알바 본인 확정 근무 */
export function useMyScheduleQuery(date: string) {
  return useQuery<MyAssignmentSlot[], ApiError>({
    queryKey: assignmentKeys.mine(date),
    queryFn: () => assignmentApi.listMine(date),
    enabled: Boolean(date),
  })
}

/** POST /api/groups/{groupId}/assignments — 성공 시 해당 날짜 배정 캐시 무효화 */
export function useCreateAssignmentMutation(groupId: number) {
  const queryClient = useQueryClient()

  return useMutation<CreateAssignmentResponse, ApiError, CreateAssignmentRequest>({
    mutationFn: (body) => assignmentApi.create(groupId, body),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: assignmentKeys.byDate(groupId, variables.workDate),
      })
    },
  })
}
