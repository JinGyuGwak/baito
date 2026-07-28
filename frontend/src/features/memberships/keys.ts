/** memberships 도메인 쿼리키 팩토리. */
export const membershipKeys = {
  all: ['memberships'] as const,
  groups: () => [...membershipKeys.all, 'groups'] as const,
  /** 그룹 소속 알바생 목록 (점주) */
  groupMembers: (groupId: number) => [...membershipKeys.all, 'groupMembers', groupId] as const,
}
