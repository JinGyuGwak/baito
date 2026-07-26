/** 전역 공용 API 타입. 특정 도메인에 속하지 않는 것만 여기에 둡니다. */

/** 회원 역할. OWNER=사장, PART_TIMER=알바 */
export type Role = 'OWNER' | 'PART_TIMER'

/**
 * 백엔드 공통 에러 응답 형식.
 * `code` 는 프론트가 분기에 사용할 수 있는 기계식 문자열입니다.
 * (예: VALIDATION_ERROR, AUTHENTICATION_FAILED, STAFF_QUOTA_EXCEEDED ...)
 */
export interface ApiError {
  code: string
  message: string
}

/** 대표 에러 코드 상수. 분기 시 문자열 하드코딩 대신 사용하세요. */
export const ErrorCode = {
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  AUTHENTICATION_FAILED: 'AUTHENTICATION_FAILED',
  NOT_GROUP_OWNER: 'NOT_GROUP_OWNER',
  NOT_GROUP_MEMBER: 'NOT_GROUP_MEMBER',
  MEMBER_NOT_FOUND: 'MEMBER_NOT_FOUND',
  GROUP_NOT_FOUND: 'GROUP_NOT_FOUND',
  DUPLICATE_LOGIN_ID: 'DUPLICATE_LOGIN_ID',
  SHIFT_NOT_AVAILABLE: 'SHIFT_NOT_AVAILABLE',
  STAFF_QUOTA_EXCEEDED: 'STAFF_QUOTA_EXCEEDED',
} as const

export type ErrorCode = (typeof ErrorCode)[keyof typeof ErrorCode]
