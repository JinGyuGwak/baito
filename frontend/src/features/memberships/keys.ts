/** memberships 도메인 쿼리키 팩토리. */
export const membershipKeys = {
  all: ['memberships'] as const,
  groups: () => [...membershipKeys.all, 'groups'] as const,
}
