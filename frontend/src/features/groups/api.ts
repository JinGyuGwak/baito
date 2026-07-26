import { apiClient } from '@/lib/api-client'
import type { CreateGroupRequest, CreateGroupResponse, Group } from './types'

export const groupApi = {
  /** GET /api/groups — 내가 소유한 그룹 목록 (OWNER) */
  list: async (): Promise<Group[]> => {
    const { data } = await apiClient.get<Group[]>('/groups')
    return data
  },

  /** POST /api/groups — 그룹 생성 (OWNER) */
  create: async (body: CreateGroupRequest): Promise<CreateGroupResponse> => {
    const { data } = await apiClient.post<CreateGroupResponse>('/groups', body)
    return data
  },
}
