import { useEffect, useMemo, useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Spinner } from '@/components/spinner'
import { IconCheck, IconChevR, IconClock, IconDrag, IconSparkle, IconWarn } from '@/components/icons'
import { useGroupsQuery } from '@/features/groups'
import { useAssignmentsQuery, buildCoverageBlocks } from '@/features/assignments'
import {
  useRequiredStaffQuery,
  useSetRequiredStaffMutation,
} from '@/features/schedule'
import { ScheduleTimeline, type SlotSelection } from '@/features/schedule/components/ScheduleTimeline'
import { ScheduleTabs } from '@/features/schedule/components/ScheduleTabs'
import { DateNavigator } from '@/features/schedule/components/DateNavigator'
import { formatDayTitle, toISODate } from '@/features/schedule/lib/date'
import {
  SLOT_COUNT,
  START_HOUR,
  countsToIntervals,
  slotToTime,
  slotsToCounts,
  timeToSlot,
} from '@/features/schedule/lib/slots'

/** 점주 스케줄 작성 — 날짜별 필요 인원을 30분 슬롯으로 설정. */
export function SchedulePage() {
  const { groupId: groupIdParam } = useParams()
  const groupId = Number(groupIdParam)

  const groups = useGroupsQuery()
  const groupName = groups.data?.find((g) => g.id === groupId)?.name ?? '店舗'

  const [searchParams, setSearchParams] = useSearchParams()
  const date = searchParams.get('date') ?? toISODate(new Date())
  const setDate = (d: string) => setSearchParams({ date: d }, { replace: true })

  const query = useRequiredStaffQuery(groupId, date)
  const assignments = useAssignmentsQuery(groupId, date)
  const save = useSetRequiredStaffMutation(groupId)
  const [confirmOpen, setConfirmOpen] = useState(false)

  // 서버 데이터 기준선. 편집은 로컬 counts 로 하고 저장 시 구간으로 합쳐 보낸다.
  const baseline = useMemo(() => slotsToCounts(query.data ?? []), [query.data])
  const [counts, setCounts] = useState<number[]>(baseline)
  const [selection, setSelection] = useState<SlotSelection | null>(null)
  const [draftCount, setDraftCount] = useState(1)

  // 날짜 변경 / 서버 데이터 갱신 시 로컬 상태 리셋.
  useEffect(() => {
    setCounts(baseline)
    setSelection(null)
  }, [baseline])

  // 선택 구간이 바뀌면 스텝퍼 기본값을 해당 구간의 현재 인원으로.
  useEffect(() => {
    if (selection) setDraftCount(Math.max(1, counts[selection.start] || 0))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selection?.start, selection?.end])

  const dirty = useMemo(() => counts.some((c, i) => c !== baseline[i]), [counts, baseline])

  // 기존(baseline)과 변경(counts) 필요인원의 합집합을 연속 구간으로 나눈다.
  // 한 구간 안에서 슬롯 유무가 달라지면(슬롯이 사라져 블록이 줄거나, 새 슬롯이 붙어 블록이 커지면)
  // 그 블록은 모양이 바뀐 것이므로 블록 전체가 초기화 대상이 된다. 서버도 같은 방식으로 동작하므로,
  // 그 블록의 기존 슬롯에 배정된 모든 알바를 저장 전 경고한다.
  // (블록 전체가 취소되므로 시간대는 표시하지 않고 알바 단위로 한 번씩만 보여준다.)
  const affected = useMemo(() => {
    const affectedSlots = new Array<boolean>(SLOT_COUNT).fill(false)
    const inUnion = (k: number) => baseline[k] > 0 || counts[k] > 0
    let i = 0
    while (i < SLOT_COUNT) {
      if (!inUnion(i)) {
        i++
        continue
      }
      let end = i
      while (end + 1 < SLOT_COUNT && inUnion(end + 1)) end++
      // 이 연속 구간에서 기존/변경의 슬롯 유무가 하나라도 다르면 블록 모양이 바뀐 것.
      let shapeChanged = false
      for (let k = i; k <= end; k++) {
        if ((baseline[k] > 0) !== (counts[k] > 0)) shapeChanged = true
      }
      // 배정은 기존 슬롯(baseline > 0)에만 존재할 수 있으므로 그 슬롯만 대상으로 표시.
      if (shapeChanged) for (let k = i; k <= end; k++) if (baseline[k] > 0) affectedSlots[k] = true
      i = end + 1
    }

    const byMember = new Map<number, string>()
    for (const a of assignments.data ?? []) {
      const slot = timeToSlot(a.startTime)
      if (slot >= 0 && affectedSlots[slot] && !byMember.has(a.memberId)) {
        byMember.set(a.memberId, a.memberName ?? 'アルバイト')
      }
    }
    return [...byMember.entries()]
      .map(([memberId, name]) => ({ memberId, name }))
      .sort((x, y) => x.name.localeCompare(y.name))
  }, [assignments.data, counts, baseline])

  // 슬롯별 배정된 회원 — 필요 인원 블록에 배정 현황(아바타/충원 상태)을 겹쳐 보여준다.
  const assignedBySlot = useMemo(() => {
    const arr: { memberId: number; label: string }[][] = Array.from({ length: SLOT_COUNT }, () => [])
    for (const a of assignments.data ?? []) {
      const i = timeToSlot(a.startTime)
      if (i >= 0) arr[i].push({ memberId: a.memberId, label: a.memberName ?? String(a.memberId) })
    }
    return arr
  }, [assignments.data])

  // 배정 탭과 동일한 커버리지 블록(충원 완료/부족 + 아바타)을 편집 중인 counts 기준으로 계산.
  const { blocks: displayBlocks, totalNeeded, totalFilled, shortBlocks } = useMemo(
    () => buildCoverageBlocks(counts, assignedBySlot),
    [counts, assignedBySlot],
  )

  const applyCount = (value: number) => {
    if (!selection) return
    setCounts((prev) => {
      const next = [...prev]
      for (let i = selection.start; i < selection.end; i++) next[i] = value
      return next
    })
  }

  const doSave = () => save.mutate({ workDate: date, intervals: countsToIntervals(counts) })

  const handleSave = () => {
    if (!dirty || save.isPending) return
    // 배정이 초기화되는 슬롯이 있으면 확인 모달을 먼저 띄운다.
    if (affected.length > 0) {
      setConfirmOpen(true)
      return
    }
    doSave()
  }

  const confirmSave = () => {
    setConfirmOpen(false)
    doSave()
  }

  // 취소: 변경 작업을 되돌려 원래 값을 유지한다.
  const cancelSave = () => {
    setConfirmOpen(false)
    setCounts(baseline)
    setSelection(null)
  }

  const now = new Date()
  const nowSlot =
    date === toISODate(now)
      ? (now.getHours() - START_HOUR) * 2 + now.getMinutes() / 30
      : null

  if (!Number.isFinite(groupId)) {
    return <CenteredMessage icon={<IconWarn size={28} />} title="不正なアクセス" text="グループが見つかりません。" />
  }

  return (
    <div className="px-8 pb-10 pt-6">
      <ScheduleTabs groupId={groupId} date={date} active="schedule" />

      {/* 헤더 */}
      <div className="mb-4 flex items-end justify-between">
        <div>
          <div className="mb-1.5 flex items-center gap-2 text-xs text-muted-foreground">
            <span>{groupName}</span>
            <IconChevR size={12} />
            <span className="font-bold text-foreground">シフト作成</span>
          </div>
          <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">{formatDayTitle(date)}</h1>
          <p className="mt-1 text-sm text-muted-foreground">必要な人数を30分単位でドラッグして設定してください</p>
          {totalNeeded > 0 && (
            <div className="mt-1.5 flex items-center gap-2 text-sm">
              <span className="font-bold text-[#047857]">● 充足 {totalFilled} / {totalNeeded}</span>
              {shortBlocks > 0 && (
                <>
                  <span className="text-[#D1D6DB]">·</span>
                  <span className="font-bold text-[#B0750A]">● {shortBlocks}件の時間帯で人数不足</span>
                </>
              )}
            </div>
          )}
        </div>
        <div className="flex items-center gap-2">
          <Button variant="secondary" disabled title="準備中" className="font-bold">
            <IconSparkle size={14} /> AI自動割り当て
          </Button>
          <Button
            variant="secondary"
            onClick={() => {
              setCounts(baseline)
              setSelection(null)
            }}
            disabled={!dirty}
            className="font-bold"
          >
            リセット
          </Button>
          <Button onClick={handleSave} disabled={!dirty || save.isPending} className="font-bold">
            {save.isPending ? (
              <>
                <Spinner /> 保存中…
              </>
            ) : (
              <>
                <IconCheck size={16} stroke={2.5} /> 保存
              </>
            )}
          </Button>
        </div>
      </div>

      {save.isError && (
        <div className="mb-4 flex items-center gap-2 rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]">
          <IconWarn size={16} stroke={2} />
          {save.error.message}
        </div>
      )}
      {save.isSuccess && !dirty && (
        <div className="mb-4 flex items-center gap-2 rounded-xl bg-[hsl(var(--success-bg))] px-3.5 py-3 text-[13px] font-medium text-[hsl(var(--success))]">
          <IconCheck size={16} stroke={2.5} /> 保存しました。
        </div>
      )}

      <div className="grid grid-cols-1 items-start gap-4 lg:grid-cols-[240px_1fr_280px]">
        {/* 좌: 날짜 네비게이터 */}
        <DateNavigator date={date} onChange={setDate} />

        {/* 중: 타임라인 */}
        <div className="flex flex-col gap-2.5">
          <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
            <LegendDot color="#10B981" label="充足済み" />
            <LegendDot color="#F59E0B" label="人数不足" />
            <LegendDot color="#B0B8C1" label="必要人数未設定の割り当て" />
            <span className="ml-auto flex items-center gap-1.5">
              <IconClock size={14} /> 30分単位 · 営業時間 08:00 〜 22:00
            </span>
          </div>
          {query.isPending ? (
            <div className="h-[784px] animate-pulse rounded-2xl border border-border bg-secondary/40" />
          ) : query.isError ? (
            <CenteredMessage
              icon={<IconWarn size={28} />}
              title="読み込めませんでした"
              text={query.error.message}
            />
          ) : (
            <ScheduleTimeline
              blocks={displayBlocks}
              selection={selection}
              onSelectionChange={setSelection}
              nowSlot={nowSlot}
            />
          )}
          <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <IconDrag size={14} /> 空いている領域をドラッグして時間帯を、ステッパーで人数を決めてから保存してください
          </div>
        </div>

        {/* 우: 선택 구간 상세 */}
        <DetailPanel
          selection={selection}
          draftCount={draftCount}
          setDraftCount={setDraftCount}
          onApply={applyCount}
        />
      </div>

      <ResetAssignmentDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        affected={affected}
        onConfirm={confirmSave}
        onCancel={cancelSave}
      />
    </div>
  )
}

/* ── 배정 초기화 확인 모달 ── */
function ResetAssignmentDialog({
  open,
  onOpenChange,
  affected,
  onConfirm,
  onCancel,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  affected: { memberId: number; name: string }[]
  onConfirm: () => void
  onCancel: () => void
}) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md rounded-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <span className="grid h-8 w-8 place-items-center rounded-lg bg-[#FFECEE] text-[#F04452]">
              <IconWarn size={18} stroke={2} />
            </span>
            割り当て情報がリセットされます
          </DialogTitle>
          <DialogDescription className="pt-1">
            変更しようとしている時間帯には、すでに割り当てられたアルバイトがいます。時間を変更すると、その勤務ブロックの割り当てがすべてリセットされます。続けますか？
          </DialogDescription>
        </DialogHeader>

        <div className="max-h-52 overflow-y-auto rounded-xl bg-secondary/60 p-3 text-[13px]">
          <ul className="flex flex-col gap-1.5">
            {affected.map((r) => (
              <li key={r.memberId} className="flex items-center gap-2">
                <span className="grid h-6 w-6 shrink-0 place-items-center rounded-full bg-primary/10 text-[11px] font-bold text-primary">
                  {r.name.slice(0, 1)}
                </span>
                <span className="font-semibold">{r.name}</span>
              </li>
            ))}
          </ul>
        </div>

        <DialogFooter className="gap-2 sm:gap-2">
          <Button variant="secondary" onClick={onCancel} className="font-bold">
            キャンセル
          </Button>
          <Button
            onClick={onConfirm}
            className="bg-[#F04452] font-bold hover:bg-[#D93A47] focus-visible:ring-[#F04452]"
          >
            続行
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

/* ── 선택 구간 상세 패널 ── */
function DetailPanel({
  selection,
  draftCount,
  setDraftCount,
  onApply,
}: {
  selection: SlotSelection | null
  draftCount: number
  setDraftCount: (n: number) => void
  onApply: (value: number) => void
}) {
  if (!selection) {
    return (
      <div className="rounded-2xl border border-border bg-card p-5 text-center text-sm text-muted-foreground">
        タイムラインをドラッグして
        <br />
        時間帯を選択してください
      </div>
    )
  }

  const slots = selection.end - selection.start
  const minutes = slots * 30
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  const duration = [hours > 0 ? `${hours}時間` : '', mins > 0 ? `${mins}分` : ''].filter(Boolean).join(' ')

  return (
    <div className="rounded-2xl border border-border bg-card p-[18px]">
      <span className="inline-flex h-[26px] items-center rounded-full bg-[#EEF3FF] px-2.5 text-xs font-semibold text-primary">
        選択した時間帯
      </span>
      <div className="mt-2.5 text-lg font-extrabold tracking-[-0.02em]">
        {slotToTime(selection.start)} — {slotToTime(selection.end)}
      </div>
      <div className="mt-0.5 text-[13px] text-muted-foreground">
        {duration} · 30分 × {slots} スロット
      </div>

      <div className="my-4 h-px bg-border" />

      <div className="text-[13px] font-semibold text-muted-foreground">必要人数</div>
      <div className="mt-2 flex items-center gap-2">
        <StepButton onClick={() => setDraftCount(Math.max(0, draftCount - 1))}>−</StepButton>
        <div className="flex-1 text-center text-lg font-extrabold">{draftCount}名</div>
        <StepButton onClick={() => setDraftCount(draftCount + 1)}>+</StepButton>
      </div>

      <Button onClick={() => onApply(draftCount)} className="mt-4 w-full font-bold">
        この時間帯に{draftCount}名を適用
      </Button>
      <Button
        variant="ghost"
        onClick={() => onApply(0)}
        className="mt-2 w-full text-[13px] font-semibold text-[#F04452] hover:text-[#F04452]"
      >
        この時間帯を空にする
      </Button>
    </div>
  )
}

function StepButton({ children, onClick }: { children: React.ReactNode; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="grid h-9 w-9 place-items-center rounded-lg bg-secondary text-lg font-bold text-foreground hover:bg-[#E8EBED]"
    >
      {children}
    </button>
  )
}

function LegendDot({ color, label }: { color: string; label: string }) {
  return (
    <span className="inline-flex items-center gap-1.5">
      <span className="h-1.5 w-1.5 rounded-full" style={{ background: color }} />
      {label}
    </span>
  )
}

function CenteredMessage({ icon, title, text }: { icon: React.ReactNode; title: string; text: string }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-border bg-card py-16 text-center">
      <span className="mb-3 grid h-12 w-12 place-items-center rounded-2xl bg-[#FFECEE] text-[#F04452]">{icon}</span>
      <div className="font-extrabold">{title}</div>
      <p className="mt-1 text-sm text-muted-foreground">{text}</p>
    </div>
  )
}
