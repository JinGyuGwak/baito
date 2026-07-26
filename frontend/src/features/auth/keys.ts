/** auth 도메인 쿼리키 팩토리. 문자열 하드코딩 대신 항상 이걸 경유하세요. */
export const authKeys = {
  all: ['auth'] as const,
  me: () => [...authKeys.all, 'me'] as const,
}
