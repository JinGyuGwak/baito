import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ApiError } from '@/types/api'
import { assignmentApi } from '../api'
import { assignmentKeys } from '../keys'
import type {
  AssignmentCandidate,
  AssignmentSlot,
  CancelAssignmentRequest,
  CancelAssignmentResponse,
  CreateAssignmentRequest,
  CreateAssignmentResponse,
  MyAssignmentSlot,
} from '../types'

/** GET /api/groups/{groupId}/assignments?date=... */
export function useAssignmentsQuery(groupId: number, date: string) {
  return useQuery<AssignmentSlot[], ApiError>({
    queryKey: assignmentKeys.byDate(groupId, date),
    queryFn: () => assignmentApi.list(groupId, date),
    enabled: Boolean(groupId && date),
  })
}

/** GET /api/groups/{groupId}/assignments/candidates — 선택한 시간대의 배정 가능 알바 */
export function useAssignmentCandidatesQuery(
  groupId: number,
  date: string,
  range: { startTime: string; endTime: string } | null,
) {
  return useQuery<AssignmentCandidate[], ApiError>({
    queryKey: assignmentKeys.candidates(
      groupId,
      date,
      range?.startTime ?? '',
      range?.endTime ?? '',
    ),
    queryFn: () => assignmentApi.listCandidates(groupId, date, range!.startTime, range!.endTime),
    enabled: Boolean(groupId && date && range),
  })
}

/** GET /api/me/assignments?date=... — 알바 본인 확정 근무 */
export function useMyScheduleQuery(date: string) {
  return useQuery<MyAssignmentSlot[], ApiError>({
    queryKey: assignmentKeys.mine(date),
    queryFn: () => assignmentApi.listMine(date),
    enabled: Boolean(date),
  })
}

/** POST /api/groups/{groupId}/assignments — 성공 시 해당 그룹 배정/후보 캐시 무효화 */
export function useCreateAssignmentMutation(groupId: number) {
  const queryClient = useQueryClient()

  return useMutation<CreateAssignmentResponse, ApiError, CreateAssignmentRequest>({
    mutationFn: (body) => assignmentApi.create(groupId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: assignmentKeys.byGroup(groupId) })
    },
  })
}

/** 결과: 몇 명 성공/실패했는지 + 실패 상세. all-or-nothing은 슬롯 단위(회원별 개별 요청). */
export interface AssignMembersResult {
  assignedMembers: number
  failures: { memberId: number; name: string; error: ApiError }[]
}

export interface AssignMembersVariables {
  members: { memberId: number; name: string }[]
  workDate: string
  startTime: string
  endTime: string
}

/**
 * 선택한 알바 여러 명을 같은 시간대에 순차 배정.
 * 일부만 실패할 수 있으므로 실패 목록을 결과로 돌려주고, 끝나면 배정/후보 캐시를 무효화한다.
 */
export function useAssignMembersMutation(groupId: number) {
  const queryClient = useQueryClient()

  return useMutation<AssignMembersResult, ApiError, AssignMembersVariables>({
    mutationFn: async ({ members, workDate, startTime, endTime }) => {
      const failures: AssignMembersResult['failures'] = []
      let assignedMembers = 0
      for (const member of members) {
        try {
          await assignmentApi.create(groupId, {
            memberId: member.memberId,
            workDate,
            startTime,
            endTime,
          })
          assignedMembers++
        } catch (e) {
          failures.push({ memberId: member.memberId, name: member.name, error: e as ApiError })
        }
      }
      return { assignedMembers, failures }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: assignmentKeys.byGroup(groupId) })
    },
  })
}

/** POST /api/groups/{groupId}/assignments/cancel — 성공 시 배정/후보 캐시 무효화 */
export function useCancelAssignmentMutation(groupId: number) {
  const queryClient = useQueryClient()

  return useMutation<CancelAssignmentResponse, ApiError, CancelAssignmentRequest>({
    mutationFn: (body) => assignmentApi.cancel(groupId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: assignmentKeys.byGroup(groupId) })
    },
  })
}
