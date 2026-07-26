/** POST /api/groups — 요청 */
export interface CreateGroupRequest {
  name: string
  /** 선택, 최대 255자 */
  description?: string
}

/** POST /api/groups — 응답 */
export interface CreateGroupResponse {
  groupId: number
}

/** GET /api/groups — 응답 요소 (사장이 소유한 그룹) */
export interface Group {
  id: number
  name: string
  description: string
  /** ISO-8601 */
  createdAt: string
}
