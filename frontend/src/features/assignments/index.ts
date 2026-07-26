/** assignments 도메인 public API. */
export {
  useAssignmentsQuery,
  useCreateAssignmentMutation,
  useMyScheduleQuery,
} from './hooks/useAssignments'
export { assignmentKeys } from './keys'
export { computeCoverage, type Coverage } from './lib/coverage'
export type {
  AssignmentSlot,
  CreateAssignmentRequest,
  CreateAssignmentResponse,
  MyAssignmentSlot,
} from './types'
