import { useEffect, useMemo, useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/spinner'
import { IconCheck, IconChevR, IconClock, IconSparkle, IconWarn, IconX } from '@/components/icons'
import { ErrorCode } from '@/types/api'
import { useGroupsQuery } from '@/features/groups'
import { useRequiredStaffQuery } from '@/features/schedule'
import {
  useAssignmentsQuery,
  useAssignmentCandidatesQuery,
  useAssignMembersMutation,
  useCancelAssignmentMutation,
  buildCoverageBlocks,
  type AssignmentCandidate,
} from '@/features/assignments'
import { ScheduleTimeline } from '@/features/schedule/components/ScheduleTimeline'
import { ScheduleTabs } from '@/features/schedule/components/ScheduleTabs'
import { formatDayTitle, toISODate } from '@/features/schedule/lib/date'
import {
  SLOT_COUNT,
  START_HOUR,
  slotToTime,
  slotsToCounts,
  timeToSlot,
} from '@/features/schedule/lib/slots'

interface SelectedBlock {
  key: string | number
  startSlot: number
  endSlot: number
}

/**
 * 점주 알바생 배정 — 필요 인원 탭에서 설정한 시간대 블록을 클릭해 선택하고,
 * 해당 시간대에 근무 가능한 알바생 목록에서 골라 배정/배정취소한다.
 */
export function AssignmentPage() {
  const { groupId: groupIdParam } = useParams()
  const groupId = Number(groupIdParam)

  const groups = useGroupsQuery()
  const groupName = groups.data?.find((g) => g.id === groupId)?.name ?? '店舗'

  const [searchParams] = useSearchParams()
  const date = searchParams.get('date') ?? toISODate(new Date())

  const required = useRequiredStaffQuery(groupId, date)
  const assignments = useAssignmentsQuery(groupId, date)

  const [selected, setSelected] = useState<SelectedBlock | null>(null)

  // 날짜가 바뀌면 블록 선택 해제.
  useEffect(() => {
    setSelected(null)
  }, [date])

  // 슬롯별 필요 인원 / 배정된 회원.
  const requiredCounts = useMemo(() => slotsToCounts(required.data ?? []), [required.data])
  const assignedBySlot = useMemo(() => {
    const arr: { memberId: number; label: string }[][] = Array.from({ length: SLOT_COUNT }, () => [])
    for (const a of assignments.data ?? []) {
      const i = timeToSlot(a.startTime)
      if (i >= 0) arr[i].push({ memberId: a.memberId, label: a.memberName ?? String(a.memberId) })
    }
    return arr
  }, [assignments.data])

  const { blocks, totalNeeded, totalFilled, shortBlocks } = useMemo(
    () => buildCoverageBlocks(requiredCounts, assignedBySlot),
    [requiredCounts, assignedBySlot],
  )

  // 선택된 블록이 데이터 갱신으로 사라지면 해제.
  useEffect(() => {
    if (selected && !blocks.some((b) => b.key === selected.key)) setSelected(null)
  }, [blocks, selected])

  // 선택된 블록 범위에 현재 배정된 알바(고유 회원) — 배정취소 대상.
  const assignedInBlock = useMemo(() => {
    if (!selected) return []
    const byId = new Map<number, { memberId: number; label: string; slotCount: number }>()
    for (let i = selected.startSlot; i < selected.endSlot; i++) {
      for (const m of assignedBySlot[i]) {
        const entry = byId.get(m.memberId)
        if (entry) entry.slotCount++
        else byId.set(m.memberId, { ...m, slotCount: 1 })
      }
    }
    return [...byId.values()].sort((a, b) => a.label.localeCompare(b.label))
  }, [selected, assignedBySlot])

  const isLoading = required.isPending || assignments.isPending
  const loadError = required.isError ? required.error : assignments.isError ? assignments.error : null

  const now = new Date()
  const nowSlot =
    date === toISODate(now) ? (now.getHours() - START_HOUR) * 2 + now.getMinutes() / 30 : null

  if (!Number.isFinite(groupId)) {
    return <CenteredMessage title="不正なアクセス" text="グループが見つかりません。" />
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
            <span className="font-bold text-foreground">アルバイト割り当て</span>
          </div>
          <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">{formatDayTitle(date)} — アルバイト割り当て</h1>
          <div className="mt-1 flex items-center gap-2 text-sm">
            <span className="font-bold text-[#047857]">● 充足 {totalFilled} / {totalNeeded}</span>
            {shortBlocks > 0 && (
              <>
                <span className="text-[#D1D6DB]">·</span>
                <span className="font-bold text-[#B0750A]">● {shortBlocks}件の時間帯で人数不足</span>
              </>
            )}
          </div>
        </div>
        <Button variant="secondary" disabled title="準備中" className="font-bold">
          <IconSparkle size={14} /> AI自動割り当て
        </Button>
      </div>

      <div className="grid grid-cols-1 items-start gap-4 lg:grid-cols-[1fr_340px]">
        {/* 좌: 범례 + 타임라인 */}
        <div className="flex flex-col gap-2.5">
          <div className="flex flex-wrap items-center gap-2 text-xs">
            <Legend color="#10B981" label="充足済み" />
            <Legend color="#F59E0B" label="人数不足" />
            <Legend color="#B0B8C1" label="必要人数未設定の割り当て" />
            <span className="ml-auto flex items-center gap-1.5 text-muted-foreground">
              <IconClock size={14} /> 時間帯ブロックをクリックして選択
            </span>
          </div>
          {isLoading ? (
            <div className="h-[784px] animate-pulse rounded-2xl border border-border bg-secondary/40" />
          ) : loadError ? (
            <CenteredMessage title="読み込めませんでした" text={loadError.message} />
          ) : blocks.length === 0 ? (
            <CenteredMessage
              title="設定された必要人数がありません"
              text="必要人数タブで、先に時間帯ごとの必要人数を設定してください。"
            />
          ) : (
            <ScheduleTimeline
              blocks={blocks}
              onBlockClick={(b) =>
                setSelected((prev) =>
                  prev?.key === b.key ? null : { key: b.key, startSlot: b.startSlot, endSlot: b.endSlot },
                )
              }
              activeBlockKey={selected?.key ?? null}
              nowSlot={nowSlot}
            />
          )}
        </div>

        {/* 우: 배정 패널 */}
        <AssignPanel
          groupId={groupId}
          date={date}
          selected={selected}
          assignedInBlock={assignedInBlock}
        />
      </div>
    </div>
  )
}

/* ── 우측 배정 패널 ─────────────────────────────────────────── */

function AssignPanel({
  groupId,
  date,
  selected,
  assignedInBlock,
}: {
  groupId: number
  date: string
  selected: SelectedBlock | null
  assignedInBlock: { memberId: number; label: string; slotCount: number }[]
}) {
  const range = selected
    ? { startTime: slotToTime(selected.startSlot), endTime: slotToTime(selected.endSlot) }
    : null

  const candidates = useAssignmentCandidatesQuery(groupId, date, range)
  const assignMembers = useAssignMembersMutation(groupId)
  const cancel = useCancelAssignmentMutation(groupId)

  const [checked, setChecked] = useState<Set<number>>(new Set())

  // 블록이 바뀌면 체크/결과 초기화.
  useEffect(() => {
    setChecked(new Set())
    assignMembers.reset()
    cancel.reset()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selected?.key])

  const toggle = (c: AssignmentCandidate) => {
    if (c.alreadyAssigned) return
    setChecked((prev) => {
      const next = new Set(prev)
      if (next.has(c.memberId)) next.delete(c.memberId)
      else next.add(c.memberId)
      return next
    })
  }

  const handleAssign = () => {
    if (!range || checked.size === 0 || assignMembers.isPending) return
    const members = (candidates.data ?? [])
      .filter((c) => checked.has(c.memberId))
      .map((c) => ({ memberId: c.memberId, name: c.name }))
    assignMembers.mutate(
      { members, workDate: date, startTime: range.startTime, endTime: range.endTime },
      { onSuccess: () => setChecked(new Set()) },
    )
  }

  const handleCancelAssignment = (memberId: number) => {
    if (!range || cancel.isPending) return
    cancel.mutate({ memberId, workDate: date, startTime: range.startTime, endTime: range.endTime })
  }

  return (
    <div className="rounded-2xl border border-border bg-card p-[18px]">
      <div className="text-[15px] font-extrabold">アルバイト割り当て</div>
      <p className="mt-1 text-xs leading-relaxed text-muted-foreground">
        タイムラインで時間帯ブロックを選択すると、その時間に勤務可能なアルバイトが表示されます。
      </p>

      <div className="my-4 h-px bg-border" />

      {!selected || !range ? (
        <div className="rounded-xl bg-[hsl(var(--field))] px-3.5 py-6 text-center text-sm text-muted-foreground">
          時間帯ブロックをクリックして選択してください
        </div>
      ) : (
        <>
          <div className="rounded-xl bg-[hsl(var(--field))] px-3.5 py-2.5 text-sm font-bold">
            {range.startTime} — {range.endTime}
            <span className="ml-1.5 font-medium text-muted-foreground">
              ({selected.endSlot - selected.startSlot}スロット)
            </span>
          </div>

          {/* 근무 가능한 알바생 */}
          <div className="mt-4">
            <div className="mb-2 text-[13px] font-semibold text-muted-foreground">
              勤務可能なアルバイト
              {candidates.data && candidates.data.length > 0 && ` · ${candidates.data.length}名`}
            </div>

            {candidates.isPending ? (
              <div className="flex flex-col gap-2">
                <div className="h-14 animate-pulse rounded-xl bg-secondary/50" />
                <div className="h-14 animate-pulse rounded-xl bg-secondary/50" />
              </div>
            ) : candidates.isError ? (
              <div className="rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]">
                {candidates.error.message}
              </div>
            ) : candidates.data.length === 0 ? (
              <div className="rounded-xl bg-[hsl(var(--field))] px-3.5 py-5 text-center text-[13px] text-muted-foreground">
                この時間帯すべてに勤務可能なアルバイトがいません。
              </div>
            ) : (
              <div className="flex flex-col gap-1.5">
                {candidates.data.map((c) => (
                  <CandidateRow
                    key={c.memberId}
                    candidate={c}
                    checked={checked.has(c.memberId)}
                    onToggle={() => toggle(c)}
                  />
                ))}
              </div>
            )}
          </div>

          {/* 배정 실행 결과 */}
          {assignMembers.data && (
            <div className="mt-3 flex flex-col gap-1.5">
              {assignMembers.data.assignedMembers > 0 && (
                <div className="rounded-xl bg-[hsl(var(--success-bg))] px-3.5 py-3 text-[13px] font-medium text-[hsl(var(--success))]">
                  {assignMembers.data.assignedMembers}名を割り当てました。
                </div>
              )}
              {assignMembers.data.failures.map((f) => (
                <div
                  key={f.memberId}
                  className="flex items-start gap-2 rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]"
                >
                  <span className="mt-0.5">
                    <IconWarn size={16} stroke={2} />
                  </span>
                  <span>
                    <b>{f.name}</b>: {toAssignMessage(f.error.code, f.error.message)}
                  </span>
                </div>
              ))}
            </div>
          )}

          <Button
            onClick={handleAssign}
            disabled={checked.size === 0 || assignMembers.isPending}
            className="mt-4 w-full font-bold"
          >
            {assignMembers.isPending ? (
              <>
                <Spinner /> 割り当て中…
              </>
            ) : (
              `この時間帯に割り当て${checked.size > 0 ? ` (${checked.size}名)` : ''}`
            )}
          </Button>

          {/* 배정된 알바생 (배정취소) */}
          <div className="my-4 h-px bg-border" />
          <div className="mb-2 text-[13px] font-semibold text-muted-foreground">
            この時間帯に割り当てられたアルバイト
            {assignedInBlock.length > 0 && ` · ${assignedInBlock.length}名`}
          </div>
          {assignedInBlock.length === 0 ? (
            <div className="rounded-xl bg-[hsl(var(--field))] px-3.5 py-4 text-center text-[13px] text-muted-foreground">
              まだ割り当てられたアルバイトがいません。
            </div>
          ) : (
            <div className="flex flex-col gap-1.5">
              {assignedInBlock.map((m) => (
                <div
                  key={m.memberId}
                  className="flex items-center gap-2.5 rounded-xl border border-border px-3 py-2.5"
                >
                  <MemberAvatar id={m.memberId} label={m.label} />
                  <div className="flex-1 text-[13px] font-bold">{m.label}</div>
                  <Button
                    variant="ghost"
                    onClick={() => handleCancelAssignment(m.memberId)}
                    disabled={cancel.isPending}
                    className="h-7 px-2 text-xs font-semibold text-[#F04452] hover:text-[#F04452]"
                  >
                    {cancel.isPending && cancel.variables?.memberId === m.memberId ? (
                      <Spinner size={13} />
                    ) : (
                      <>
                        <IconX size={13} /> 割り当て解除
                      </>
                    )}
                  </Button>
                </div>
              ))}
            </div>
          )}
          {cancel.isError && (
            <div className="mt-2 rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]">
              {cancel.error.message}
            </div>
          )}
          {cancel.isSuccess && (
            <div className="mt-2 rounded-xl bg-[hsl(var(--success-bg))] px-3.5 py-3 text-[13px] font-medium text-[hsl(var(--success))]">
              {cancel.data.cancelledSlotCount}スロットの割り当てを解除しました。
            </div>
          )}
        </>
      )}
    </div>
  )
}

/** 후보 한 명 — 선택 가능 / 이미 배정(선택 불가) 상태를 시각적으로 구분한다. */
function CandidateRow({
  candidate,
  checked,
  onToggle,
}: {
  candidate: AssignmentCandidate
  checked: boolean
  onToggle: () => void
}) {
  const disabled = candidate.alreadyAssigned
  const intervals = candidate.availableIntervals
    .map((iv) => `${iv.startTime.slice(0, 5)}–${iv.endTime.slice(0, 5)}`)
    .join(', ')

  return (
    <button
      type="button"
      onClick={onToggle}
      disabled={disabled}
      aria-pressed={checked}
      className={[
        'flex w-full items-center gap-2.5 rounded-xl border px-3 py-2.5 text-left',
        disabled
          ? 'cursor-not-allowed border-border bg-secondary/50 opacity-60'
          : checked
            ? 'border-primary bg-[#EEF3FF]'
            : 'border-border bg-white hover:border-[#C5D1FF]',
      ].join(' ')}
    >
      {/* 체크 표시 */}
      <span
        className={[
          'grid h-[18px] w-[18px] shrink-0 place-items-center rounded-md border-2',
          disabled
            ? 'border-[#D1D6DB] bg-[#E8EBED]'
            : checked
              ? 'border-primary bg-primary text-white'
              : 'border-[#D1D6DB] bg-white',
        ].join(' ')}
      >
        {checked && !disabled && <IconCheck size={12} stroke={3} />}
      </span>

      <MemberAvatar id={candidate.memberId} label={candidate.name} muted={disabled} />

      <div className="min-w-0 flex-1">
        <div className="truncate text-[13px] font-bold">
          {candidate.name}
          <span className="ml-1 font-medium text-muted-foreground">({candidate.loginId})</span>
        </div>
        <div className="truncate text-[11px] text-muted-foreground">可能: {intervals || '—'}</div>
      </div>

      {disabled && (
        <span className="shrink-0 rounded-full bg-[#E8EBED] px-2 py-0.5 text-[11px] font-bold text-[#6B7684]">
          割り当て済み
        </span>
      )}
    </button>
  )
}

function MemberAvatar({ id, label, muted }: { id: number; label: string; muted?: boolean }) {
  return (
    <span
      className="grid h-7 w-7 shrink-0 place-items-center rounded-full text-[11px] font-bold text-white"
      style={{ background: muted ? '#B0B8C1' : `hsl(${(id * 73) % 360} 60% 62%)` }}
    >
      {label.slice(0, 1)}
    </span>
  )
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
      return 'このアルバイトは、その時間を勤務可能時間として登録していません。'
    case ErrorCode.STAFF_QUOTA_EXCEEDED:
      return fallback // 서버 메시지가 이미 구체적("필요 인원이 모두 배정됨")
    case ErrorCode.NOT_GROUP_MEMBER:
      return 'このグループに所属するアルバイトではありません。'
    case ErrorCode.MEMBER_NOT_FOUND:
      return '存在しない会員です。'
    default:
      return fallback
  }
}
