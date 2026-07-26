/**
 * 환경변수 접근을 한 곳에 모읍니다.
 * import.meta.env 를 컴포넌트/모듈에서 직접 읽지 말고 항상 이 파일을 경유하세요.
 */
export const env = {
  /** API 베이스 URL. 개발 환경에서는 Vite dev proxy 를 태우기 위해 '/api' 를 사용합니다. */
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api',
} as const
