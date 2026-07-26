import { useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { IconBell, IconClock, IconWarn } from '@/components/icons'
import { useMyGroupsQuery } from '@/features/memberships'
import { useMyScheduleQuery } from '@/features/assignments'
import { ScheduleTimeline, type TimelineBlock } from '@/features/schedule/components/ScheduleTimeline'
import { DateNavigator } from '@/features/schedule/components/DateNavigator'
import { formatDayTitle, toISODate } from '@/features/schedule/lib/date'
import { SLOT_COUNT, START_HOUR, booleansToBlocks, slotToTime, timeToSlot } from '@/features/schedule/lib/slots'

interface ShiftBlock {
  groupId: number
  groupName: string
  startSlot: number
  endSlot: number
}

/** 알바 내 스케줄 — 날짜별 확정된 근무(전체 소속 그룹)를 읽기 전용으로 표시. */
export function MySchedulePage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const date = searchParams.get('date') ?? toISODate(new Date())
  const setDate = (d: string) => setSearchParams({ date: d }, { replace: true })

  const myGroups = useMyGroupsQuery()
  const schedule = useMyScheduleQuery(date)

  const groupName = (id: number) => myGroups.data?.find((g) => g.id === id)?.name ?? `그룹 #${id}`

  // 그룹별로 슬롯을 모아 연속 구간(블록)으로 합침.
  const shifts = useMemo<ShiftBlock[]>(() => {
    const byGroup = new Map<number, boolean[]>()
    for (const a of schedule.data ?? []) {
      const i = timeToSlot(a.startTime)
      if (i < 0) continue
      if (!byGroup.has(a.groupId)) byGroup.set(a.groupId, new Array(SLOT_COUNT).fill(false))
      byGroup.get(a.groupId)![i] = true
    }
    const out: ShiftBlock[] = []
    for (const [groupId, bools] of byGroup) {
      for (const b of booleansToBlocks(bools)) {
        out.push({ groupId, groupName: groupName(groupId), startSlot: b.startSlot, endSlot: b.endSlot })
      }
    }
    return out.sort((a, b) => a.startSlot - b.startSlot)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [schedule.data, myGroups.data])

  const timelineBlocks: TimelineBlock[] = shifts.map((s) => ({
    key: `${s.groupId}-${s.startSlot}`,
    startSlot: s.startSlot,
    endSlot: s.endSlot,
    tone: 'primary',
    title: s.groupName,
    sub: `${slotToTime(s.startSlot)} – ${slotToTime(s.endSlot)} · 배정 완료`,
  }))

  const totalSlots = schedule.data?.length ?? 0
  const now = new Date()
  const nowSlot =
    date === toISODate(now) ? (now.getHours() - START_HOUR) * 2 + now.getMinutes() / 30 : null

  return (
    <div className="px-8 pb-10 pt-6">
      <div className="mb-4">
        <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">내 스케줄</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          {formatDayTitle(date)} · 확정된 근무{' '}
          <b className="text-foreground">{formatDuration(totalSlots)}</b>
        </p>
      </div>

      <div className="grid grid-cols-1 items-start gap-4 lg:grid-cols-[240px_1fr_280px]">
        {/* 좌: 날짜 + 요약 */}
        <div className="flex flex-col gap-3">
          <DateNavigator date={date} onChange={setDate} />
          <div className="rounded-2xl border border-border bg-card p-4">
            <div className="mb-2.5 text-[13px] font-extrabold">이 날 요약</div>
            {shifts.length === 0 ? (
              <p className="text-[13px] text-muted-foreground">확정된 근무가 없어요.</p>
            ) : (
              <div className="flex flex-col gap-2 text-[13px]">
                {shifts.map((s) => (
                  <div key={`${s.groupId}-${s.startSlot}`} className="flex items-center justify-between">
                    <span className="text-[#4E5968]">{s.groupName}</span>
                    <span className="font-bold">
                      {slotToTime(s.startSlot)}–{slotToTime(s.endSlot)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* 중: 타임라인 */}
        <div className="flex flex-col gap-2.5">
          <div className="flex items-center justify-end gap-1.5 text-xs text-muted-foreground">
            <IconClock size={14} /> 운영 시간 08:00 ~ 22:00
          </div>
          {schedule.isPending ? (
            <div className="h-[784px] animate-pulse rounded-2xl border border-border bg-secondary/40" />
          ) : schedule.isError ? (
            <Centered title="불러오지 못했어요" text={schedule.error.message} />
          ) : (
            <ScheduleTimeline blocks={timelineBlocks} selection={null} onSelectionChange={() => {}} nowSlot={nowSlot} readOnly />
          )}
        </div>

        {/* 우: 확정 근무 목록 */}
        <div className="rounded-2xl border border-border bg-card p-[18px]">
          <span className="inline-flex h-[26px] items-center rounded-full bg-[#EEF3FF] px-2.5 text-xs font-semibold text-primary">
            확정된 근무
          </span>
          {shifts.length === 0 ? (
            <p className="mt-3 text-sm text-muted-foreground">
              아직 이 날 배정된 근무가 없어요. 점주님이 배정하면 여기에 표시돼요.
            </p>
          ) : (
            <div className="mt-3 flex flex-col gap-2">
              {shifts.map((s) => (
                <div
                  key={`${s.groupId}-${s.startSlot}`}
                  className="rounded-[10px] border border-[#DCE5FF] bg-[#EEF3FF] px-3 py-2.5"
                >
                  <div className="text-[13px] font-bold text-primary">{s.groupName}</div>
                  <div className="text-[12px] font-medium text-primary/80">
                    {slotToTime(s.startSlot)} – {slotToTime(s.endSlot)}
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="mt-4 flex items-start gap-2.5 rounded-xl bg-secondary px-3.5 py-3">
            <span className="mt-0.5 text-primary">
              <IconBell size={16} />
            </span>
            <p className="text-[12px] leading-relaxed text-muted-foreground">
              점주님이 배정을 변경하면 자동으로 반영돼요. 대타 요청 기능은 준비 중이에요.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

function Centered({ title, text }: { title: string; text: string }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-border bg-card py-16 text-center">
      <span className="mb-3 grid h-12 w-12 place-items-center rounded-2xl bg-[#FFECEE] text-[#F04452]">
        <IconWarn size={28} />
      </span>
      <div className="font-extrabold">{title}</div>
      <p className="mt-1 text-sm text-muted-foreground">{text}</p>
    </div>
  )
}

function formatDuration(slots: number): string {
  const minutes = slots * 30
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0 && m === 0) return '없음'
  return [h > 0 ? `${h}시간` : '', m > 0 ? `${m}분` : ''].filter(Boolean).join(' ')
}
