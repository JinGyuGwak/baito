import { Button } from '@/components/ui/button'
import { IconChevL, IconChevR } from '@/components/icons'
import { formatDayTitle, shiftISODate, toISODate } from '../lib/date'

/** 날짜 이동 카드 — 이전/다음 날, date 입력, 오늘로 이동. */
export function DateNavigator({ date, onChange }: { date: string; onChange: (d: string) => void }) {
  return (
    <div className="rounded-2xl border border-border bg-card p-4">
      <div className="mb-3 flex items-center justify-between">
        <button
          type="button"
          onClick={() => onChange(shiftISODate(date, -1))}
          className="grid h-8 w-8 place-items-center rounded-lg text-muted-foreground hover:bg-secondary"
          aria-label="이전 날"
        >
          <IconChevL size={14} />
        </button>
        <div className="text-sm font-extrabold">{formatDayTitle(date)}</div>
        <button
          type="button"
          onClick={() => onChange(shiftISODate(date, 1))}
          className="grid h-8 w-8 place-items-center rounded-lg text-muted-foreground hover:bg-secondary"
          aria-label="다음 날"
        >
          <IconChevR size={14} />
        </button>
      </div>
      <input
        type="date"
        value={date}
        onChange={(e) => e.target.value && onChange(e.target.value)}
        className="h-10 w-full rounded-xl border border-transparent bg-[hsl(var(--field))] px-3 text-sm outline-none focus:border-primary focus:bg-background"
      />
      <Button
        variant="ghost"
        onClick={() => onChange(toISODate(new Date()))}
        className="mt-2 h-9 w-full text-[13px] font-semibold"
      >
        오늘로 이동
      </Button>
    </div>
  )
}
