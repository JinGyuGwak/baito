/** invitations 도메인 쿼리키 팩토리. */
export const invitationKeys = {
  all: ['invitations'] as const,
  sent: () => [...invitationKeys.all, 'sent'] as const,
  received: () => [...invitationKeys.all, 'received'] as const,
}
