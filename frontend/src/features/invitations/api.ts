import { apiClient } from '@/lib/api-client'
import type { CreateInvitationRequest, CreateInvitationResponse, Invitation } from './types'

export const invitationApi = {
  /** POST /api/invitations — 초대 생성 (OWNER) */
  create: async (body: CreateInvitationRequest): Promise<CreateInvitationResponse> => {
    const { data } = await apiClient.post<CreateInvitationResponse>('/invitations', body)
    return data
  },

  /** GET /api/invitations/sent — 보낸 초대 목록 (OWNER) */
  listSent: async (): Promise<Invitation[]> => {
    const { data } = await apiClient.get<Invitation[]>('/invitations/sent')
    return data
  },

  /** GET /api/invitations/received — 받은 초대 목록, PENDING 만 (PART_TIMER) */
  listReceived: async (): Promise<Invitation[]> => {
    const { data } = await apiClient.get<Invitation[]>('/invitations/received')
    return data
  },

  /** POST /api/invitations/{id}/cancel — 취소 (OWNER), 204 */
  cancel: async (invitationId: number): Promise<void> => {
    await apiClient.post(`/invitations/${invitationId}/cancel`)
  },

  /** POST /api/invitations/{id}/accept — 수락 (PART_TIMER), 204 */
  accept: async (invitationId: number): Promise<void> => {
    await apiClient.post(`/invitations/${invitationId}/accept`)
  },

  /** POST /api/invitations/{id}/reject — 거절 (PART_TIMER), 204 */
  reject: async (invitationId: number): Promise<void> => {
    await apiClient.post(`/invitations/${invitationId}/reject`)
  },
}
