import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'
import { scheduleApi } from '../api'
import { scheduleKeys } from '../keys'
import type { AvailabilitySlot, SetAvailabilityRequest } from '../types'

/** GET /api/groups/{groupId}/availability?date=... */
export function useAvailabilityQuery(groupId: number, date: string) {
  return useQuery<AvailabilitySlot[], ApiError>({
    queryKey: scheduleKeys.availabilityByDate(groupId, date),
    queryFn: () => scheduleApi.getAvailability(groupId, date),
    enabled: Boolean(groupId && date),
  })
}

/** PUT /api/groups/{groupId}/availability — 성공 시 해당 날짜 캐시 무효화 */
export function useSetAvailabilityMutation(groupId: number) {
  const queryClient = useQueryClient()

  return useMutation<void, ApiError, SetAvailabilityRequest>({
    mutationFn: (body) => scheduleApi.setAvailability(groupId, body),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: scheduleKeys.availabilityByDate(groupId, variables.workDate),
      })
    },
  })
}
