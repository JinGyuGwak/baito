import { Link, useLocation } from 'react-router-dom'
import { IconBell, IconPlus, IconSettings } from '@/components/icons'

export interface SidebarGroup {
  id: number
  name: string
}

interface SidebarProps {
  title: string
  groups: SidebarGroup[]
  /** 그룹 클릭 시 이동할 경로 생성기 */
  groupHref: (groupId: number) => string
  /** "새 그룹" 액션 경로 (점주만). 없으면 항목 숨김. */
  createHref?: string
}

/** 그룹 색상 팔레트 (디자인의 그룹 뱃지 톤). */
const GROUP_TONES = [
  { bg: '#EEF3FF', fg: '#3D6AFF' },
  { bg: '#FFF3EC', fg: '#E66A2C' },
  { bg: '#E7F8F1', fg: '#10B981' },
  { bg: '#F2EDFE', fg: '#8B5CF6' },
  { bg: '#FDE7F2', fg: '#EC4899' },
]

export function Sidebar({ title, groups, groupHref, createHref }: SidebarProps) {
  const { pathname } = useLocation()

  return (
    <aside className="border-r border-border bg-white p-4 pt-6">
      <div className="px-3 pb-2 text-[11px] font-extrabold uppercase tracking-[0.06em] text-muted-foreground">
        {title}
      </div>

      {groups.map((g, i) => {
        const href = groupHref(g.id)
        const active = pathname === href
        const tone = GROUP_TONES[i % GROUP_TONES.length]
        return (
          <Link
            key={g.id}
            to={href}
            className={[
              'flex items-center gap-2.5 rounded-[10px] px-3 py-2.5 text-sm font-semibold',
              active ? 'bg-[#EEF3FF] text-primary' : 'text-[#4E5968] hover:bg-secondary',
            ].join(' ')}
          >
            <span
              className="grid h-[26px] w-[26px] place-items-center rounded-lg text-[13px] font-extrabold"
              style={{ background: tone.bg, color: tone.fg }}
            >
              {g.name.slice(0, 1)}
            </span>
            <span className="overflow-hidden text-ellipsis whitespace-nowrap">{g.name}</span>
          </Link>
        )
      })}

      {createHref && (
        <Link
          to={createHref}
          className="flex items-center gap-2.5 rounded-[10px] px-3 py-2.5 text-sm font-semibold text-muted-foreground hover:bg-secondary"
        >
          <span className="grid h-[26px] w-[26px] place-items-center rounded-lg border-[1.5px] border-dashed border-[#D1D6DB]">
            <IconPlus size={14} />
          </span>
          新しいグループを作成
        </Link>
      )}

      <div className="mt-8 px-3 pb-2 text-[11px] font-extrabold uppercase tracking-[0.06em] text-muted-foreground">
        一般
      </div>
      <div className="flex items-center gap-2.5 rounded-[10px] px-3 py-2.5 text-sm font-semibold text-[#4E5968]">
        <IconBell size={16} />
        通知
      </div>
      <div className="flex items-center gap-2.5 rounded-[10px] px-3 py-2.5 text-sm font-semibold text-[#4E5968]">
        <IconSettings size={16} />
        設定
      </div>
    </aside>
  )
}
