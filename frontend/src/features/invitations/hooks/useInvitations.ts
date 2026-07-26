import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'
import { membershipKeys } from '@/features/memberships/keys'
import { invitationApi } from '../api'
import { invitationKeys } from '../keys'
import type { CreateInvitationRequest, CreateInvitationResponse, Invitation } from '../types'

/** GET /api/invitations/sent — 보낸 초대 목록 (OWNER) */
export function useSentInvitationsQuery() {
  return useQuery<Invitation[], ApiError>({
    queryKey: invitationKeys.sent(),
    queryFn: invitationApi.listSent,
  })
}

/** GET /api/invitations/received — 받은 초대 목록 (PART_TIMER) */
export function useReceivedInvitationsQuery() {
  return useQuery<Invitation[], ApiError>({
    queryKey: invitationKeys.received(),
    queryFn: invitationApi.listReceived,
  })
}

/** POST /api/invitations — 초대 생성 (OWNER) */
export function useCreateInvitationMutation() {
  const queryClient = useQueryClient()

  return useMutation<CreateInvitationResponse, ApiError, CreateInvitationRequest>({
    mutationFn: invitationApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invitationKeys.sent() })
    },
  })
}

/** POST /api/invitations/{id}/cancel — 취소 (OWNER) */
export function useCancelInvitationMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ApiError, number>({
    mutationFn: invitationApi.cancel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invitationKeys.sent() })
    },
  })
}

/**
 * POST /api/invitations/{id}/accept — 수락 (PART_TIMER)
 * 수락 시 그룹 멤버십이 생성되므로 받은 초대 목록 + 가입 그룹 목록을 함께 무효화.
 */
export function useAcceptInvitationMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ApiError, number>({
    mutationFn: invitationApi.accept,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invitationKeys.received() })
      queryClient.invalidateQueries({ queryKey: membershipKeys.groups() })
    },
  })
}

/** POST /api/invitations/{id}/reject — 거절 (PART_TIMER) */
export function useRejectInvitationMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ApiError, number>({
    mutationFn: invitationApi.reject,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: invitationKeys.received() })
    },
  })
}
