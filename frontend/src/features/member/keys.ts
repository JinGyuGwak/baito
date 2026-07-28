/** member 도메인 쿼리키 팩토리. */
export const memberKeys = {
  all: ['member'] as const,
  me: () => [...memberKeys.all, 'me'] as const,
}
