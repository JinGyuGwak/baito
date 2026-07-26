/**
 * schedule 도메인 쿼리키 팩토리.
 * 중첩 리소스이므로 부모 groupId 와 조회 날짜(date)를 키에 포함합니다.
 */
export const scheduleKeys = {
  all: ['schedule'] as const,

  availability: (groupId: number) =>
    [...scheduleKeys.all, 'availability', groupId] as const,
  availabilityByDate: (groupId: number, date: string) =>
    [...scheduleKeys.availability(groupId), date] as const,

  requiredStaff: (groupId: number) =>
    [...scheduleKeys.all, 'required-staff', groupId] as const,
  requiredStaffByDate: (groupId: number, date: string) =>
    [...scheduleKeys.requiredStaff(groupId), date] as const,
}
