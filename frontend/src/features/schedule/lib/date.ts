/** 스케줄 화면 공통 날짜 유틸. */

const WEEKDAYS = ['日', '月', '火', '水', '木', '金', '土']

/** Date → "yyyy-MM-dd" (로컬 기준). */
export function toISODate(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** "yyyy-MM-dd" → "M월 D일 (요일)". */
export function formatDayTitle(iso: string): string {
  const d = new Date(iso + 'T00:00:00')
  if (Number.isNaN(d.getTime())) return iso
  return `${d.getMonth() + 1}月${d.getDate()}日 (${WEEKDAYS[d.getDay()]})`
}

/** iso 날짜에 days 를 더한 "yyyy-MM-dd". */
export function shiftISODate(iso: string, days: number): string {
  const d = new Date(iso + 'T00:00:00')
  d.setDate(d.getDate() + days)
  return toISODate(d)
}
