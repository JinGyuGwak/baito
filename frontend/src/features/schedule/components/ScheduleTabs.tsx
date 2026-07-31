import { Link } from 'react-router-dom'

type Tab = 'schedule' | 'assignments' | 'invitations' | 'members'

/**
 * 점주 그룹 관리 상단 탭 — 필요 인원 / 알바생 배정 / 알바생 초대관리 / 알바생 관리.
 * 날짜(date)를 유지한 채 전환한다.
 */
export function ScheduleTabs({ groupId, date, active }: { groupId: number; date: string; active: Tab }) {
  const tabs: { key: Tab; label: string; to: string }[] = [
    { key: 'schedule', label: '필요 인원', to: `/groups/${groupId}/schedule?date=${date}` },
    { key: 'assignments', label: '알바생 배정', to: `/groups/${groupId}/assignments?date=${date}` },
    { key: 'invitations', label: '알바생 초대관리', to: `/groups/${groupId}/invitations` },
    { key: 'members', label: '알바생 관리', to: `/groups/${groupId}/members` },
  ]
  return (
    <div className="mb-5 flex gap-1.5 border-b border-border">
      {tabs.map((t) => (
        <Link
          key={t.key}
          to={t.to}
          className={[
            '-mb-px border-b-2 px-4 py-3 text-sm font-bold',
            t.key === active
              ? 'border-foreground text-foreground'
              : 'border-transparent text-muted-foreground hover:text-foreground',
          ].join(' ')}
        >
          {t.label}
        </Link>
      ))}
    </div>
  )
}
