import type { SentInvitationParams } from './types'

/** invitations 도메인 쿼리키 팩토리. */
export const invitationKeys = {
  all: ['invitations'] as const,
  sent: () => [...invitationKeys.all, 'sent'] as const,
  sentList: (params: SentInvitationParams) =>
    [
      ...invitationKeys.sent(),
      params.groupId,
      params.status ?? 'ALL',
      params.page ?? 0,
      params.size ?? 10,
    ] as const,
  received: () => [...invitationKeys.all, 'received'] as const,
}
