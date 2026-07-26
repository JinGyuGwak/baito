/** assignments 도메인 쿼리키 팩토리. 부모 groupId + 날짜 포함. */
export const assignmentKeys = {
  all: ['assignments'] as const,
  byGroup: (groupId: number) => [...assignmentKeys.all, groupId] as const,
  byDate: (groupId: number, date: string) =>
    [...assignmentKeys.byGroup(groupId), date] as const,
  /** 알바 본인 확정 근무(날짜별, 전체 그룹) */
  mine: (date: string) => [...assignmentKeys.all, 'mine', date] as const,
}
