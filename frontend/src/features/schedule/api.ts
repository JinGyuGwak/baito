import { apiClient } from '@/lib/api-client'
import type {
  AvailabilitySlot,
  RequiredStaffSlot,
  SetAvailabilityRequest,
  SetRequiredStaffRequest,
} from './types'

export const scheduleApi = {
  // ── 근무 가능 시간 (PART_TIMER) ──
  /** PUT /api/groups/{groupId}/availability — 204 */
  setAvailability: async (groupId: number, body: SetAvailabilityRequest): Promise<void> => {
    await apiClient.put(`/groups/${groupId}/availability`, body)
  },
  /** GET /api/groups/{groupId}/availability?date=yyyy-MM-dd */
  getAvailability: async (groupId: number, date: string): Promise<AvailabilitySlot[]> => {
    const { data } = await apiClient.get<AvailabilitySlot[]>(
      `/groups/${groupId}/availability`,
      { params: { date } },
    )
    return data
  },

  // ── 필요 인원 (OWNER) ──
  /** PUT /api/groups/{groupId}/required-staff — 204 */
  setRequiredStaff: async (groupId: number, body: SetRequiredStaffRequest): Promise<void> => {
    await apiClient.put(`/groups/${groupId}/required-staff`, body)
  },
  /** GET /api/groups/{groupId}/required-staff?date=yyyy-MM-dd */
  getRequiredStaff: async (groupId: number, date: string): Promise<RequiredStaffSlot[]> => {
    const { data } = await apiClient.get<RequiredStaffSlot[]>(
      `/groups/${groupId}/required-staff`,
      { params: { date } },
    )
    return data
  },
}
