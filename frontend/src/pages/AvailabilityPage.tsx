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
  const groupName = myGroups.data?.find((g) => g.id === groupId)?.name ?? '매장'

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
    title: '근무 가능',
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
    return <CenteredMessage title="잘못된 접근" text="그룹을 찾을 수 없어요." />
  }

  return (
    <div className="px-8 pb-10 pt-6">
      {/* 헤더 */}
      <div className="mb-4 flex items-end justify-between gap-4">
        <div>
          <div className="mb-1.5 flex items-center gap-2 text-xs text-muted-foreground">
            <span>{groupName}</span>
            <IconChevR size={12} />
            <span className="font-bold text-foreground">근무 가능 시간</span>
          </div>
          <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">근무 가능 시간 설정</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            언제 일할 수 있는지만 알려주면, 점주님이 알맞은 시간대에 배정해드려요
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
                <Spinner /> 저장 중…
              </>
            ) : (
              <>
                <IconCheck size={16} stroke={2.5} /> 저장
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
          <IconCheck size={16} stroke={2.5} /> 저장했어요.
        </div>
      )}

      <div className="grid grid-cols-1 items-start gap-4 lg:grid-cols-[240px_1fr_280px]">
        {/* 좌: 날짜 + 빠른 설정 + 팁 */}
        <div className="flex flex-col gap-3">
          <DateNavigator date={date} onChange={setDate} />
          <div className="rounded-2xl border border-border bg-card p-4">
            <div className="mb-2.5 text-[13px] font-extrabold">빠른 설정</div>
            <div className="flex flex-col gap-1.5">
              <QuickButton onClick={() => setBools(new Array(SLOT_COUNT).fill(true))}>
                <IconCheck size={14} /> 이 날 전체 가능
              </QuickButton>
              <QuickButton onClick={() => setBools(new Array(SLOT_COUNT).fill(false))}>
                <IconX size={14} /> 이 날 불가
              </QuickButton>
            </div>
          </div>
          <div className="rounded-2xl border border-[#DCE5FF] bg-[linear-gradient(160deg,#EEF3FF,#F8FAFF)] p-4">
            <div className="mb-2 flex items-center gap-2 text-[13px] font-extrabold">
              <IconSparkle size={16} stroke={2} /> 꿀팁
            </div>
            <p className="text-xs leading-relaxed text-[#4E5968]">
              넓게 등록할수록 배정받을 확률이 높아져요. 점주님은 이 시간 안에서만 배정할 수 있어요.
            </p>
          </div>
        </div>

        {/* 중: 타임라인 */}
        <div className="flex flex-col gap-2.5">
          <div className="flex items-center justify-end gap-1.5 text-xs text-muted-foreground">
            <IconClock size={14} /> 30분 단위 · 운영 시간 08:00 ~ 22:00
          </div>
          {query.isPending ? (
            <div className="h-[784px] animate-pulse rounded-2xl border border-border bg-secondary/40" />
          ) : query.isError ? (
            <CenteredMessage title="불러오지 못했어요" text={query.error.message} />
          ) : (
            <ScheduleTimeline
              blocks={timelineBlocks}
              selection={selection}
              onSelectionChange={setSelection}
              nowSlot={nowSlot}
            />
          )}
          <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <IconDrag size={14} /> 빈 영역을 드래그해 시간대를 고른 뒤 아래에서 추가하세요
          </div>
        </div>

        {/* 우: 선택 적용 + 오늘의 등록 */}
        <div className="flex flex-col gap-3">
          {selection && (
            <div className="rounded-2xl border border-border bg-card p-[18px]">
              <div className="text-[13px] font-semibold text-muted-foreground">선택한 시간대</div>
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
                <IconCheck size={16} stroke={2.5} /> 근무 가능으로 추가
              </Button>
              <Button
                variant="ghost"
                onClick={() => {
                  paint(selection, false)
                  setSelection(null)
                }}
                className="mt-2 w-full text-[13px] font-semibold text-[#F04452] hover:text-[#F04452]"
              >
                이 시간대 제거
              </Button>
            </div>
          )}

          <div className="rounded-2xl border border-border bg-card p-[18px]">
            <span className="inline-flex h-[26px] items-center rounded-full bg-[#E7F8F1] px-2.5 text-xs font-semibold text-[#047857]">
              오늘의 등록
            </span>
            <div className="mt-2.5 text-lg font-extrabold">{totalLabel} 가능</div>
            <div className="mt-0.5 text-[13px] text-muted-foreground">{blocks.length}개 시간대 등록됨</div>

            <div className="my-4 h-px bg-border" />

            {blocks.length === 0 ? (
              <p className="text-sm text-muted-foreground">아직 등록한 시간이 없어요. 드래그해서 추가해보세요.</p>
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
                      aria-label="삭제"
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
  if (h === 0 && m === 0) return '0시간'
  return [h > 0 ? `${h}시간` : '', m > 0 ? `${m}분` : ''].filter(Boolean).join(' ')
}
