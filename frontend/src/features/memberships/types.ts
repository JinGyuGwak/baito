/** GET /api/memberships/groups — 응답 요소 (알바가 소속된 그룹) */
export interface MembershipGroup {
  id: number
  name: string
  description: string
}

/** GET /api/groups/{groupId}/members — 응답 요소 (그룹 소속 알바생, 점주 조회) */
export interface GroupMember {
  memberId: number
  name: string
  loginId: string
  /** ISO-8601 그룹 합류 일시 */
  joinedAt: string
}
