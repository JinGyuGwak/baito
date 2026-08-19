import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { assignmentKeys } from '@/features/assignments'
import type { ApiError } from '@/types/api'
import { scheduleApi } from '../api'
import { scheduleKeys } from '../keys'
import type { RequiredStaffSlot, SetRequiredStaffRequest } from '../types'

/** GET /api/groups/{groupId}/required-staff?date=... */
export function useRequiredStaffQuery(groupId: number, date: string) {
  return useQuery<RequiredStaffSlot[], ApiError>({
    queryKey: scheduleKeys.requiredStaffByDate(groupId, date),
    queryFn: () => scheduleApi.getRequiredStaff(groupId, date),
    enabled: Boolean(groupId && date),
  })
}

/** PUT /api/groups/{groupId}/required-staff — 성공 시 해당 날짜 캐시 무효화 */
export function useSetRequiredStaffMutation(groupId: number) {
  const queryClient = useQueryClient()

  return useMutation<void, ApiError, SetRequiredStaffRequest>({
    mutationFn: (body) => scheduleApi.setRequiredStaff(groupId, body),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: scheduleKeys.requiredStaffByDate(groupId, variables.workDate),
      })
      // 시간대 변경으로 서버가 배정을 취소했을 수 있으므로 배정/후보 캐시도 무효화한다.
      // (안 그러면 배정 탭에서 새로고침 전까지 취소된 알바가 남아 보인다.)
      queryClient.invalidateQueries({ queryKey: assignmentKeys.byGroup(groupId) })
    },
  })
}
