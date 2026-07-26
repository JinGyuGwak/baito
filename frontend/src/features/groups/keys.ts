/** groups 도메인 쿼리키 팩토리. 넓은 것 → 좁은 것 순서로 구성. */
export const groupKeys = {
  all: ['groups'] as const,
  lists: () => [...groupKeys.all, 'list'] as const,
  detail: (groupId: number) => [...groupKeys.all, 'detail', groupId] as const,
}
