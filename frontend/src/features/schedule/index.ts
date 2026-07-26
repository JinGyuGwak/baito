/** schedule 도메인 public API. */
export { useAvailabilityQuery, useSetAvailabilityMutation } from './hooks/useAvailability'
export { useRequiredStaffQuery, useSetRequiredStaffMutation } from './hooks/useRequiredStaff'
export { scheduleKeys } from './keys'
export type {
  TimeInterval,
  AvailabilitySlot,
  SetAvailabilityRequest,
  RequiredStaffInterval,
  RequiredStaffSlot,
  SetRequiredStaffRequest,
} from './types'
