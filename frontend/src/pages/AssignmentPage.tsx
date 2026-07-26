import { useMemo, useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Spinner } from '@/components/spinner'
import { IconChevR, IconClock, IconSparkle, IconWarn } from '@/components/icons'
import { ErrorCode } from '@/types/api'
import { useGroupsQuery } from '@/features/groups'
import { useRequiredStaffQuery } from '@/features/schedule'
import { useAssignmentsQuery, useCreateAssignmentMutation } from '@/features/assignments'
import { ScheduleTimeline, type SlotSelection, type TimelineBlock } from '@/features/schedule/components/ScheduleTimeline'
import { ScheduleTabs } from '@/features/schedule/components/ScheduleTabs'
import { formatDayTitle, toISODate } from '@/features/schedule/lib/date'
import {
  SLOT_COUNT,
  START_HOUR,
  countsToBlocks,
  slotToTime,
  slotsToCounts,
  timeToSlot,
} from '@/features/schedule/lib/slots'

/** 점주 알바생 배정 — 필요 인원 대비 배정 현황을 보고, 회원 ID로 시간대에 배정. */
export function AssignmentPage() {
  const { groupId: groupIdParam } = useParams()
  const groupId = Number(groupIdParam)

  const groups = useGroupsQuery()
  const groupName = groups.data?.find((g) => g.id === groupId)?.name ?? '매장'

  const [searchParams] = useSearchParams()
  const date = searchParams.get('date') ?? toISODate(new Date())

  const required = useRequiredStaffQuery(groupId, date)
  const assignments = useAssignmentsQuery(groupId, date)
  const create = useCreateAssignmentMutation(groupId)

  const [selection, setSelection] = useState<SlotSelection | null>(null)
  const [memberId, setMemberId] = useState('')

  // 슬롯별 필요 인원 / 배정된 회원.
  const requiredCounts = useMemo(() => slotsToCounts(required.data ?? []), [required.data])
  const assignedBySlot = useMemo(() => {
    const arr: number[][] = Array.from({ length: SLOT_COUNT }, () => [])
    for (const a of assignments.data ?? []) {
      const i = timeToSlot(a.startTime)
      if (i >= 0) arr[i].push(a.memberId)
    }
    return arr
  }, [assignments.data])

  const { blocks, totalNeeded, totalFilled, shortBlocks } = useMemo(
    () => buildBlocks(requiredCounts, assignedBySlot),
    [requiredCounts, assignedBySlot],
  )

  const parsedMemberId = Number(memberId)
  const canAssign =
    selection != null && Number.isInteger(parsedMemberId) && parsedMemberId > 0 && !create.isPending

  const handleAssign = () => {
    if (!selection || !canAssign) return
    create.mutate(
      {
        memberId: parsedMemberId,
        workDate: date,
        startTime: slotToTime(selection.start),
        endTime: slotToTime(selection.end),
      },
      {
        onSuccess: () => {
          setSelection(null)
          setMemberId('')
        },
      },
    )
  }

  const isLoading = required.isPending || assignments.isPending
  const loadError = required.isError ? required.error : assignments.isError ? assignments.error : null

  const now = new Date()
  const nowSlot =
    date === toISODate(now) ? (now.getHours() - START_HOUR) * 2 + now.getMinutes() / 30 : null

  if (!Number.isFinite(groupId)) {
    return <CenteredMessage title="잘못된 접근" text="그룹을 찾을 수 없어요." />
  }

  return (
    <div className="px-8 pb-10 pt-6">
      <ScheduleTabs groupId={groupId} date={date} active="assignments" />

      {/* 헤더 */}
      <div className="mb-4 flex items-end justify-between">
        <div>
          <div className="mb-1.5 flex items-center gap-2 text-xs text-muted-foreground">
            <span>{groupName}</span>
            <IconChevR size={12} />
            <span className="font-bold text-foreground">알바생 배정</span>
          </div>
          <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">{formatDayTitle(date)} — 알바생 배정</h1>
          <div className="mt-1 flex items-center gap-2 text-sm">
            <span className="font-bold text-[#047857]">● 채움 {totalFilled} / {totalNeeded}</span>
            {shortBlocks > 0 && (
              <>
                <span className="text-[#D1D6DB]">·</span>
                <span className="font-bold text-[#B0750A]">● {shortBlocks}개 시간대 인원 부족</span>
              </>
            )}
          </div>
        </div>
        <Button variant="secondary" disabled title="준비 중" className="font-bold">
          <IconSparkle size={14} /> AI 자동 배정
        </Button>
      </div>

      <div className="grid grid-cols-1 items-start gap-4 lg:grid-cols-[1fr_300px]">
        {/* 좌: 범례 + 타임라인 */}
        <div className="flex flex-col gap-2.5">
          <div className="flex flex-wrap items-center gap-2 text-xs">
            <Legend color="#10B981" label="충원 완료" />
            <Legend color="#F59E0B" label="인원 부족" />
            <Legend color="#B0B8C1" label="필요 인원 미설정 배정" />
            <span className="ml-auto flex items-center gap-1.5 text-muted-foreground">
              <IconClock size={14} /> 30분 단위 · 08:00 ~ 22:00
            </span>
          </div>
          {isLoading ? (
            <div className="h-[784px] animate-pulse rounded-2xl border border-border bg-secondary/40" />
          ) : loadError ? (
            <CenteredMessage title="불러오지 못했어요" text={loadError.message} />
          ) : (
            <ScheduleTimeline
              blocks={blocks}
              selection={selection}
              onSelectionChange={setSelection}
              nowSlot={nowSlot}
            />
          )}
        </div>

        {/* 우: 배정 패널 */}
        <div className="rounded-2xl border border-border bg-card p-[18px]">
          <div className="text-[15px] font-extrabold">알바생 배정</div>
          <p className="mt-1 text-xs leading-relaxed text-muted-foreground">
            타임라인을 드래그해 시간대를 고르고, 배정할 알바생의 회원 ID를 입력하세요.
          </p>

          <div className="my-4 h-px bg-border" />

          <div className="flex flex-col gap-2">
            <Label className="text-[13px] font-semibold text-muted-foreground">선택한 시간대</Label>
            {selection ? (
              <div className="rounded-xl bg-[hsl(var(--field))] px-3.5 py-2.5 text-sm font-bold">
                {slotToTime(selection.start)} — {slotToTime(selection.end)}
                <span className="ml-1.5 font-medium text-muted-foreground">
                  ({selection.end - selection.start}개 슬롯)
                </span>
              </div>
            ) : (
              <div className="rounded-xl bg-[hsl(var(--field))] px-3.5 py-2.5 text-sm text-muted-foreground">
                드래그해서 선택
              </div>
            )}
          </div>

          <div className="mt-3 flex flex-col gap-2">
            <Label htmlFor="memberId" className="text-[13px] font-semibold text-muted-foreground">
              알바 회원 ID
            </Label>
            <Input
              id="memberId"
              type="number"
              inputMode="numeric"
              min={1}
              placeholder="예: 2"
              value={memberId}
              onChange={(e) => setMemberId(e.target.value)}
            />
          </div>

          {create.isError && (
            <div className="mt-3 flex items-start gap-2 rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]">
              <span className="mt-0.5">
                <IconWarn size={16} stroke={2} />
              </span>
              {toAssignMessage(create.error.code, create.error.message)}
            </div>
          )}
          {create.isSuccess && (
            <div className="mt-3 flex items-center gap-2 rounded-xl bg-[hsl(var(--success-bg))] px-3.5 py-3 text-[13px] font-medium text-[hsl(var(--success))]">
              {create.data.assignedSlotCount}개 슬롯 배정했어요.
            </div>
          )}

          <Button onClick={handleAssign} disabled={!canAssign} className="mt-4 w-full font-bold">
            {create.isPending ? (
              <>
                <Spinner /> 배정 중…
              </>
            ) : (
              '이 시간대에 배정'
            )}
          </Button>

          <p className="mt-3 text-[11px] leading-relaxed text-muted-foreground">
            모든 슬롯이 근무 가능 시간·필요 인원 조건을 통과해야 저장돼요(all-or-nothing).
          </p>
        </div>
      </div>
    </div>
  )
}

/* 슬롯 데이터 → 타임라인 블록 + 요약 통계 */
function buildBlocks(
  requiredCounts: number[],
  assignedBySlot: number[][],
): { blocks: TimelineBlock[]; totalNeeded: number; totalFilled: number; shortBlocks: number } {
  const blocks: TimelineBlock[] = []
  let shortBlocks = 0
  let totalNeeded = 0
  let totalFilled = 0

  for (let i = 0; i < SLOT_COUNT; i++) {
    totalNeeded += requiredCounts[i]
    totalFilled += Math.min(assignedBySlot[i].length, requiredCounts[i])
  }

  // 1) 필요 인원 블록 — 배정 커버리지로 색칠
  for (const b of countsToBlocks(requiredCounts)) {
    let maxShort = 0
    const union = new Set<number>()
    for (let i = b.startSlot; i < b.endSlot; i++) {
      maxShort = Math.max(maxShort, Math.max(0, b.count - assignedBySlot[i].length))
      for (const id of assignedBySlot[i]) union.add(id)
    }
    const short = maxShort > 0
    if (short) shortBlocks++
    blocks.push({
      key: `req-${b.startSlot}`,
      startSlot: b.startSlot,
      endSlot: b.endSlot,
      tone: short ? 'warn' : 'success',
      title: `${b.count}명 필요`,
      sub: `${slotToTime(b.startSlot)} – ${slotToTime(b.endSlot)} · 배정 ${union.size}${short ? ` · ${maxShort}명 부족` : ' · 충원 완료'}`,
      avatars: [...union].map((id) => ({ id, label: String(id) })),
    })
  }

  // 2) 필요 인원 미설정인데 배정된 슬롯 — 별도 표시(연속 구간으로 합침)
  let runStart = -1
  for (let i = 0; i <= SLOT_COUNT; i++) {
    const extra = i < SLOT_COUNT && requiredCounts[i] === 0 && assignedBySlot[i].length > 0
    if (runStart >= 0 && !extra) {
      const union = new Set<number>()
      for (let j = runStart; j < i; j++) assignedBySlot[j].forEach((id) => union.add(id))
      blocks.push({
        key: `extra-${runStart}`,
        startSlot: runStart,
        endSlot: i,
        tone: 'muted',
        title: `배정 ${union.size}명`,
        sub: `${slotToTime(runStart)} – ${slotToTime(i)} · 필요 인원 미설정`,
        avatars: [...union].map((id) => ({ id, label: String(id) })),
      })
      runStart = -1
    }
    if (extra && runStart < 0) runStart = i
  }

  return { blocks, totalNeeded, totalFilled, shortBlocks }
}

function Legend({ color, label }: { color: string; label: string }) {
  return (
    <span className="inline-flex h-[26px] items-center gap-1.5 rounded-full bg-secondary px-2.5 font-semibold text-[#4E5968]">
      <span className="h-1.5 w-1.5 rounded-full" style={{ background: color }} />
      {label}
    </span>
  )
}

function CenteredMessage({ title, text }: { title: string; text: string }) {
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

/** 배정 에러 코드 → 사용자 메시지. */
function toAssignMessage(code: string, fallback: string): string {
  switch (code) {
    case ErrorCode.SHIFT_NOT_AVAILABLE:
      return '이 알바생이 해당 시간을 근무 가능 시간으로 등록하지 않았어요.'
    case ErrorCode.STAFF_QUOTA_EXCEEDED:
      return fallback // 서버 메시지가 이미 구체적("필요 인원이 모두 배정됨")
    case ErrorCode.NOT_GROUP_MEMBER:
      return '이 그룹 소속 알바생이 아니에요.'
    case ErrorCode.MEMBER_NOT_FOUND:
      return '존재하지 않는 회원이에요.'
    default:
      return fallback
  }
}
