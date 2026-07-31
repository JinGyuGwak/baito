import { apiClient } from '@/lib/api-client'
import type {
  AssignmentCandidate,
  AssignmentSlot,
  CancelAssignmentRequest,
  CancelAssignmentResponse,
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

  /** POST /api/groups/{groupId}/assignments/cancel — 범위 내 확정 슬롯 취소 (OWNER) */
  cancel: async (
    groupId: number,
    body: CancelAssignmentRequest,
  ): Promise<CancelAssignmentResponse> => {
    const { data } = await apiClient.post<CancelAssignmentResponse>(
      `/groups/${groupId}/assignments/cancel`,
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

  /** GET /api/groups/{groupId}/assignments/candidates — 시간대에 근무 가능한 알바 목록 (OWNER) */
  listCandidates: async (
    groupId: number,
    date: string,
    startTime: string,
    endTime: string,
  ): Promise<AssignmentCandidate[]> => {
    const { data } = await apiClient.get<AssignmentCandidate[]>(
      `/groups/${groupId}/assignments/candidates`,
      { params: { date, startTime, endTime } },
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
