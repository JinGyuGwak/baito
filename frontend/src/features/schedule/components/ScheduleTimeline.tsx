import { useRef } from 'react'
import { END_HOUR, SLOT_COUNT, SLOT_PX, START_HOUR, slotToTime } from '../lib/slots'

export interface SlotSelection {
  /** 시작 슬롯(포함) */
  start: number
  /** 끝 슬롯(제외) */
  end: number
}

export type BlockTone = 'primary' | 'success' | 'warn' | 'muted'

export interface TimelineBlock {
  key: string | number
  startSlot: number
  endSlot: number
  tone: BlockTone
  title: string
  sub?: string
  /** 블록에 표시할 아바타(배정된 회원 등) */
  avatars?: { id: number; label: string }[]
}

const TONE_CLASS: Record<BlockTone, string> = {
  primary: 'border-[#DCE5FF] bg-[#EEF3FF] text-primary',
  success: 'border-[#BBEFD8] bg-[#E7F8F1] text-[#047857]',
  warn: 'border-[#FFE2A8] bg-[#FFF6E5] text-[#B0750A]',
  muted: 'border border-dashed border-[#D1D6DB] bg-[#F9FAFB] text-muted-foreground',
}

interface Props {
  blocks: TimelineBlock[]
  selection?: SlotSelection | null
  onSelectionChange?: (selection: SlotSelection) => void
  /** 지정하면 드래그 대신 블록 클릭으로 선택하는 모드가 된다. */
  onBlockClick?: (block: TimelineBlock) => void
  /** 클릭 모드에서 현재 선택된 블록 key (하이라이트 표시) */
  activeBlockKey?: string | number | null
  /** 현재 시각 슬롯(소수 허용). null 이면 표시 안 함. */
  nowSlot?: number | null
  readOnly?: boolean
}

/**
 * 08:00~22:00, 30분 슬롯 세로 타임라인.
 * 기본은 빈 영역 드래그로 구간 선택, `onBlockClick` 을 주면 블록 클릭 선택 모드로 동작한다.
 */
export function ScheduleTimeline({
  blocks,
  selection,
  onSelectionChange,
  onBlockClick,
  activeBlockKey,
  nowSlot,
  readOnly,
}: Props) {
  const trackRef = useRef<HTMLDivElement>(null)
  const anchorRef = useRef<number | null>(null)

  const hours = Array.from({ length: END_HOUR - START_HOUR }, (_, i) => START_HOUR + i)

  const slotFromEvent = (clientY: number): number => {
    const el = trackRef.current
    if (!el) return 0
    const y = clientY - el.getBoundingClientRect().top
    return Math.max(0, Math.min(SLOT_COUNT - 1, Math.floor(y / SLOT_PX)))
  }

  const dragEnabled = !readOnly && !onBlockClick && onSelectionChange != null

  const handlePointerDown = (e: React.PointerEvent) => {
    if (!dragEnabled) return
    e.preventDefault()
    trackRef.current?.setPointerCapture(e.pointerId)
    const slot = slotFromEvent(e.clientY)
    anchorRef.current = slot
    onSelectionChange!({ start: slot, end: slot + 1 })
  }

  const handlePointerMove = (e: React.PointerEvent) => {
    if (!dragEnabled || anchorRef.current === null) return
    const slot = slotFromEvent(e.clientY)
    const a = anchorRef.current
    onSelectionChange!({ start: Math.min(a, slot), end: Math.max(a, slot) + 1 })
  }

  const handlePointerUp = (e: React.PointerEvent) => {
    if (anchorRef.current === null) return
    anchorRef.current = null
    trackRef.current?.releasePointerCapture(e.pointerId)
  }

  return (
    <div className="grid grid-cols-[60px_1fr] overflow-hidden rounded-2xl border border-border bg-white">
      {/* 시간 눈금 */}
      <div className="border-r border-border bg-[#FBFCFD]">
        {hours.map((h) => (
          <div
            key={h}
            className="flex items-start justify-end px-2.5 pt-1.5 text-xs font-semibold text-muted-foreground"
            style={{ height: SLOT_PX * 2 }}
          >
            {String(h).padStart(2, '0')}:00
          </div>
        ))}
      </div>

      {/* 슬롯 트랙 */}
      <div
        ref={trackRef}
        className={dragEnabled ? 'relative cursor-crosshair touch-none select-none' : 'relative'}
        style={{ height: SLOT_COUNT * SLOT_PX }}
        onPointerDown={handlePointerDown}
        onPointerMove={handlePointerMove}
        onPointerUp={handlePointerUp}
      >
        {/* 격자선 */}
        {Array.from({ length: SLOT_COUNT }, (_, i) => (
          <div
            key={i}
            className={i === 0 ? '' : i % 2 === 0 ? 'border-t border-border' : 'border-t border-dashed border-[#E8EBED]'}
            style={{ height: SLOT_PX }}
          />
        ))}

        {/* 블록 */}
        {blocks.map((b) => (
          <div
            key={b.key}
            onClick={onBlockClick ? () => onBlockClick(b) : undefined}
            className={[
              'absolute left-2 right-2 flex flex-col gap-0.5 overflow-hidden rounded-[10px] border px-3 py-2',
              TONE_CLASS[b.tone],
              onBlockClick
                ? 'cursor-pointer transition-shadow hover:shadow-md'
                : 'pointer-events-none',
              onBlockClick && activeBlockKey === b.key
                ? 'ring-2 ring-primary ring-offset-1'
                : '',
            ].join(' ')}
            style={{ top: b.startSlot * SLOT_PX, height: (b.endSlot - b.startSlot) * SLOT_PX }}
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold">{b.title}</span>
            </div>
            {b.sub && <div className="text-[11px] font-medium opacity-80">{b.sub}</div>}
            {b.avatars && b.avatars.length > 0 && (
              <div className="mt-0.5 flex">
                {b.avatars.slice(0, 6).map((a, j) => (
                  <span
                    key={a.id}
                    className="grid h-5 w-5 place-items-center rounded-full border-2 border-white text-[10px] font-bold text-white"
                    style={{ background: `hsl(${(a.id * 73) % 360} 60% 62%)`, marginLeft: j === 0 ? 0 : -8 }}
                    title={a.label}
                  >
                    {a.label.slice(0, 1)}
                  </span>
                ))}
              </div>
            )}
          </div>
        ))}

        {/* 드래그 선택 하이라이트 */}
        {selection && (
          <div
            className="pointer-events-none absolute left-2 right-2 rounded-[10px] border-2 border-primary bg-primary/20"
            style={{ top: selection.start * SLOT_PX, height: (selection.end - selection.start) * SLOT_PX }}
          >
            <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 whitespace-nowrap rounded-md bg-primary px-2 py-0.5 text-[11px] font-extrabold text-white">
              {slotToTime(selection.start)} – {slotToTime(selection.end)}
            </div>
          </div>
        )}

        {/* 현재 시각 라인 */}
        {nowSlot != null && nowSlot >= 0 && nowSlot <= SLOT_COUNT && (
          <div className="pointer-events-none absolute inset-x-0 z-[2] border-t-2 border-[#F04452]" style={{ top: nowSlot * SLOT_PX }}>
            <span className="absolute -left-1.5 -top-1.5 h-2.5 w-2.5 rounded-full bg-[#F04452]" />
          </div>
        )}
      </div>
    </div>
  )
}
