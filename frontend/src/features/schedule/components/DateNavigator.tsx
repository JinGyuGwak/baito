import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { IconChevL, IconChevR } from '@/components/icons'
import { toISODate } from '../lib/date'

const WEEKDAYS = ['日', '月', '火', '水', '木', '金', '土']

/** "yyyy-MM-dd" → 로컬 Date (00:00). */
function parseISO(iso: string): Date {
  return new Date(iso + 'T00:00:00')
}

/** 해당 월을 채우는 6주(42칸)의 날짜 배열. 앞뒤로 이웃 달 날짜 포함. */
function buildMonthGrid(year: number, month: number): Date[] {
  const first = new Date(year, month, 1)
  const start = new Date(year, month, 1 - first.getDay()) // 그 주 일요일부터
  return Array.from({ length: 42 }, (_, i) => {
    const d = new Date(start)
    d.setDate(start.getDate() + i)
    return d
  })
}

/**
 * 날짜 선택 카드 — 항상 펼쳐진 월 캘린더.
 * 선택된 날짜는 강조 표시하고, 오늘은 테두리로 구분한다.
 */
export function DateNavigator({ date, onChange }: { date: string; onChange: (d: string) => void }) {
  const selected = parseISO(date)
  const todayISO = toISODate(new Date())

  // 보이는 달(연/월). 선택 날짜가 다른 달로 바뀌면 그 달로 따라간다.
  const [view, setView] = useState({ year: selected.getFullYear(), month: selected.getMonth() })
  useEffect(() => {
    setView({ year: selected.getFullYear(), month: selected.getMonth() })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [date])

  const grid = buildMonthGrid(view.year, view.month)
  const shiftMonth = (delta: number) =>
    setView((v) => {
      const d = new Date(v.year, v.month + delta, 1)
      return { year: d.getFullYear(), month: d.getMonth() }
    })

  return (
    <div className="rounded-2xl border border-border bg-card p-4">
      {/* 월 이동 헤더 */}
      <div className="mb-3 flex items-center justify-between">
        <button
          type="button"
          onClick={() => shiftMonth(-1)}
          className="grid h-8 w-8 place-items-center rounded-lg text-muted-foreground hover:bg-secondary"
          aria-label="前の月"
        >
          <IconChevL size={14} />
        </button>
        <div className="text-sm font-extrabold">
          {view.year}年 {view.month + 1}月
        </div>
        <button
          type="button"
          onClick={() => shiftMonth(1)}
          className="grid h-8 w-8 place-items-center rounded-lg text-muted-foreground hover:bg-secondary"
          aria-label="次の月"
        >
          <IconChevR size={14} />
        </button>
      </div>

      {/* 요일 헤더 */}
      <div className="grid grid-cols-7 gap-0.5">
        {WEEKDAYS.map((w, i) => (
          <div
            key={w}
            className={`grid h-7 place-items-center text-[11px] font-bold ${
              i === 0 ? 'text-[#F04452]' : i === 6 ? 'text-primary' : 'text-muted-foreground'
            }`}
          >
            {w}
          </div>
        ))}
      </div>

      {/* 날짜 그리드 */}
      <div className="grid grid-cols-7 gap-0.5">
        {grid.map((d) => {
          const iso = toISODate(d)
          const isSelected = iso === date
          const isToday = iso === todayISO
          const isOtherMonth = d.getMonth() !== view.month
          const dow = d.getDay()

          return (
            <button
              key={iso}
              type="button"
              onClick={() => onChange(iso)}
              aria-pressed={isSelected}
              aria-label={iso}
              className={[
                'relative grid h-9 place-items-center rounded-lg text-[13px] font-semibold transition-colors',
                isSelected
                  ? 'bg-primary text-primary-foreground'
                  : isOtherMonth
                    ? 'text-muted-foreground/50 hover:bg-secondary'
                    : dow === 0
                      ? 'text-[#F04452] hover:bg-secondary'
                      : 'text-foreground hover:bg-secondary',
                !isSelected && isToday ? 'ring-1 ring-inset ring-primary' : '',
              ].join(' ')}
            >
              {d.getDate()}
            </button>
          )
        })}
      </div>

      <Button
        variant="ghost"
        onClick={() => onChange(todayISO)}
        className="mt-3 h-9 w-full text-[13px] font-semibold"
      >
        今日へ移動
      </Button>
    </div>
  )
}
