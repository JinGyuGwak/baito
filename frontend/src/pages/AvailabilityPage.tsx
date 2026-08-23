import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/spinner'
import { IconCheck, IconChevR, IconClock, IconDrag, IconSparkle, IconWarn, IconX } from '@/components/icons'
import { useMyGroupsQuery } from '@/features/memberships'
import { useAvailabilityQuery, useSetAvailabilityMutation } from '@/features/schedule'
import { ScheduleTimeline, type SlotSelection, type TimelineBlock } from '@/features/schedule/components/ScheduleTimeline'
import { DateNavigator } from '@/features/schedule/components/DateNavigator'
import { toISODate } from '@/features/schedule/lib/date'
import {
  SLOT_COUNT,
  START_HOUR,
  availabilityToBooleans,
  booleansToBlocks,
  booleansToIntervals,
  slotToTime,
} from '@/features/schedule/lib/slots'

/** 알바생 근무 가능 시간 등록 — 날짜별 가능 구간을 30분 단위로 설정. */
export function AvailabilityPage() {
  const navigate = useNavigate()
  const { groupId: groupIdParam } = useParams()
  const groupId = Number(groupIdParam)

  const myGroups = useMyGroupsQuery()
  const groupName = myGroups.data?.find((g) => g.id === groupId)?.name ?? '店舗'

  const [searchParams, setSearchParams] = useSearchParams()
  const date = searchParams.get('date') ?? toISODate(new Date())
  const setDate = (d: string) => setSearchParams({ date: d }, { replace: true })

  const query = useAvailabilityQuery(groupId, date)
  const save = useSetAvailabilityMutation(groupId)

  const baseline = useMemo(() => availabilityToBooleans(query.data ?? []), [query.data])
  const [bools, setBools] = useState<boolean[]>(baseline)
  const [selection, setSelection] = useState<SlotSelection | null>(null)

  useEffect(() => {
    setBools(baseline)
    setSelection(null)
  }, [baseline])

  const dirty = useMemo(() => bools.some((b, i) => b !== baseline[i]), [bools, baseline])
  const blocks = useMemo(() => booleansToBlocks(bools), [bools])
  const timelineBlocks: TimelineBlock[] = blocks.map((b) => ({
    key: b.startSlot,
    startSlot: b.startSlot,
    endSlot: b.endSlot,
    tone: 'success',
    title: '勤務可能',
    sub: `${slotToTime(b.startSlot)} – ${slotToTime(b.endSlot)}`,
  }))

  const totalSlots = bools.filter(Boolean).length
  const totalLabel = formatDuration(totalSlots)

  const paint = (range: SlotSelection, value: boolean) => {
    setBools((prev) => {
      const next = [...prev]
      for (let i = range.start; i < range.end; i++) next[i] = value
      return next
    })
  }

  const handleSave = () => {
    if (!dirty || save.isPending) return
    save.mutate({ workDate: date, intervals: booleansToIntervals(bools) })
  }

  const now = new Date()
  const nowSlot =
    date === toISODate(now) ? (now.getHours() - START_HOUR) * 2 + now.getMinutes() / 30 : null

  if (!Number.isFinite(groupId)) {
    return <CenteredMessage title="不正なアクセス" text="グループが見つかりません。" />
  }

  return (
    <div className="px-8 pb-10 pt-6">
      {/* 헤더 */}
      <div className="mb-4 flex items-end justify-between gap-4">
        <div>
          <div className="mb-1.5 flex items-center gap-2 text-xs text-muted-foreground">
            <span>{groupName}</span>
            <IconChevR size={12} />
            <span className="font-bold text-foreground">勤務可能時間</span>
          </div>
          <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">勤務可能時間の設定</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            いつ働けるかを伝えるだけで、オーナーが適切な時間帯に割り当てます
          </p>
        </div>
        <div className="flex items-center gap-2">
          {myGroups.data && myGroups.data.length > 1 && (
            <select
              value={groupId}
              onChange={(e) => navigate(`/groups/${e.target.value}/availability?date=${date}`)}
              className="h-11 rounded-xl border border-[#E1E4E8] bg-white px-3 pr-8 text-sm font-semibold outline-none focus:border-primary"
            >
              {myGroups.data.map((g) => (
                <option key={g.id} value={g.id}>
                  {g.name}
                </option>
              ))}
            </select>
          )}
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
          <IconWarn size={16} stroke={2} /> {save.error.message}
        </div>
      )}
      {save.isSuccess && !dirty && (
        <div className="mb-4 flex items-center gap-2 rounded-xl bg-[hsl(var(--success-bg))] px-3.5 py-3 text-[13px] font-medium text-[hsl(var(--success))]">
          <IconCheck size={16} stroke={2.5} /> 保存しました。
        </div>
      )}

      <div className="grid grid-cols-1 items-start gap-4 lg:grid-cols-[240px_1fr_280px]">
        {/* 좌: 날짜 + 빠른 설정 + 팁 */}
        <div className="flex flex-col gap-3">
          <DateNavigator date={date} onChange={setDate} />
          <div className="rounded-2xl border border-border bg-card p-4">
            <div className="mb-2.5 text-[13px] font-extrabold">クイック設定</div>
            <div className="flex flex-col gap-1.5">
              <QuickButton onClick={() => setBools(new Array(SLOT_COUNT).fill(true))}>
                <IconCheck size={14} /> この日すべて可能
              </QuickButton>
              <QuickButton onClick={() => setBools(new Array(SLOT_COUNT).fill(false))}>
                <IconX size={14} /> この日は不可
              </QuickButton>
            </div>
          </div>
          <div className="rounded-2xl border border-[#DCE5FF] bg-[linear-gradient(160deg,#EEF3FF,#F8FAFF)] p-4">
            <div className="mb-2 flex items-center gap-2 text-[13px] font-extrabold">
              <IconSparkle size={16} stroke={2} /> ワンポイント
            </div>
            <p className="text-xs leading-relaxed text-[#4E5968]">
              広く登録するほど割り当てられる可能性が高くなります。オーナーはこの時間内でのみ割り当てできます。
            </p>
          </div>
        </div>

        {/* 중: 타임라인 */}
        <div className="flex flex-col gap-2.5">
          <div className="flex items-center justify-end gap-1.5 text-xs text-muted-foreground">
            <IconClock size={14} /> 30分単位 · 営業時間 08:00 〜 22:00
          </div>
          {query.isPending ? (
            <div className="h-[784px] animate-pulse rounded-2xl border border-border bg-secondary/40" />
          ) : query.isError ? (
            <CenteredMessage title="読み込めませんでした" text={query.error.message} />
          ) : (
            <ScheduleTimeline
              blocks={timelineBlocks}
              selection={selection}
              onSelectionChange={setSelection}
              nowSlot={nowSlot}
            />
          )}
          <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <IconDrag size={14} /> 空いている領域をドラッグして時間帯を選び、下から追加してください
          </div>
        </div>

        {/* 우: 선택 적용 + 오늘의 등록 */}
        <div className="flex flex-col gap-3">
          {selection && (
            <div className="rounded-2xl border border-border bg-card p-[18px]">
              <div className="text-[13px] font-semibold text-muted-foreground">選択した時間帯</div>
              <div className="mt-1 text-lg font-extrabold">
                {slotToTime(selection.start)} — {slotToTime(selection.end)}
              </div>
              <Button
                onClick={() => {
                  paint(selection, true)
                  setSelection(null)
                }}
                className="mt-3 w-full font-bold"
              >
                <IconCheck size={16} stroke={2.5} /> 勤務可能として追加
              </Button>
              <Button
                variant="ghost"
                onClick={() => {
                  paint(selection, false)
                  setSelection(null)
                }}
                className="mt-2 w-full text-[13px] font-semibold text-[#F04452] hover:text-[#F04452]"
              >
                この時間帯を削除
              </Button>
            </div>
          )}

          <div className="rounded-2xl border border-border bg-card p-[18px]">
            <span className="inline-flex h-[26px] items-center rounded-full bg-[#E7F8F1] px-2.5 text-xs font-semibold text-[#047857]">
              今日の登録
            </span>
            <div className="mt-2.5 text-lg font-extrabold">{totalLabel} 可能</div>
            <div className="mt-0.5 text-[13px] text-muted-foreground">{blocks.length}件の時間帯を登録済み</div>

            <div className="my-4 h-px bg-border" />

            {blocks.length === 0 ? (
              <p className="text-sm text-muted-foreground">まだ登録した時間がありません。ドラッグして追加してみましょう。</p>
            ) : (
              <div className="flex flex-col gap-2">
                {blocks.map((b) => (
                  <div
                    key={b.startSlot}
                    className="flex items-center gap-2 rounded-[10px] border border-[#BBEFD8] bg-[#E7F8F1] px-3 py-2.5"
                  >
                    <span className="h-2 w-2 rounded-full bg-[#10B981]" />
                    <span className="flex-1 text-[13px] font-bold text-[#047857]">
                      {slotToTime(b.startSlot)} — {slotToTime(b.endSlot)}
                    </span>
                    <button
                      type="button"
                      onClick={() => paint({ start: b.startSlot, end: b.endSlot }, false)}
                      className="grid place-items-center rounded p-1 text-[#047857] hover:bg-[#BBEFD8]"
                      aria-label="削除"
                    >
                      <IconX size={14} />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

function QuickButton({ children, onClick }: { children: React.ReactNode; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex items-center gap-2 rounded-lg bg-secondary px-3 py-2 text-[13px] font-semibold text-[#4E5968] hover:bg-[#E8EBED]"
    >
      {children}
    </button>
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

/** 슬롯 개수 → "N시간"/"N시간 30분"/"30분". */
function formatDuration(slots: number): string {
  const minutes = slots * 30
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0 && m === 0) return '0時間'
  return [h > 0 ? `${h}時間` : '', m > 0 ? `${m}分` : ''].filter(Boolean).join(' ')
}
