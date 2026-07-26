import { apiClient } from '@/lib/api-client'
import type {
  AssignmentSlot,
  CreateAssignmentRequest,
  CreateAssignmentResponse,
  MyAssignmentSlot,
} from './types'

export const assignmentApi = {
  /** POST /api/groups/{groupId}/assignments — all-or-nothing 배정 (OWNER) */
  create: async (
    groupId: number,
    body: CreateAssignmentRequest,
  ): Promise<CreateAssignmentResponse> => {
    const { data } = await apiClient.post<CreateAssignmentResponse>(
      `/groups/${groupId}/assignments`,
      body,
    )
    return data
  },

  /** GET /api/groups/{groupId}/assignments?date=yyyy-MM-dd */
  list: async (groupId: number, date: string): Promise<AssignmentSlot[]> => {
    const { data } = await apiClient.get<AssignmentSlot[]>(
      `/groups/${groupId}/assignments`,
      { params: { date } },
    )
    return data
  },

  /** GET /api/me/assignments?date=yyyy-MM-dd — 알바 본인 확정 근무 (전체 그룹) */
  listMine: async (date: string): Promise<MyAssignmentSlot[]> => {
    const { data } = await apiClient.get<MyAssignmentSlot[]>('/me/assignments', {
      params: { date },
    })
    return data
  },
}
