/** assignments 도메인 public API. */
export {
  useAssignmentsQuery,
  useAssignmentCandidatesQuery,
  useCreateAssignmentMutation,
  useAssignMembersMutation,
  useCancelAssignmentMutation,
  useMyScheduleQuery,
  type AssignMembersResult,
} from './hooks/useAssignments'
export { assignmentKeys } from './keys'
export { computeCoverage, type Coverage } from './lib/coverage'
export type {
  AssignmentCandidate,
  AssignmentSlot,
  CancelAssignmentRequest,
  CancelAssignmentResponse,
  CreateAssignmentRequest,
  CreateAssignmentResponse,
  MyAssignmentSlot,
} from './types'
