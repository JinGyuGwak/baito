import { Link } from 'react-router-dom'
import { IconClock, IconUsers, IconWarn } from '@/components/icons'
import { useMyGroupsQuery, type MembershipGroup } from '@/features/memberships'
import { ReceivedInvitations } from '@/features/invitations'

const CARD_TONES = [
  { bg: '#EEF3FF', fg: '#3D6AFF' },
  { bg: '#F2EDFE', fg: '#8B5CF6' },
  { bg: '#FFF3EC', fg: '#E66A2C' },
  { bg: '#E7F8F1', fg: '#10B981' },
]

/** 알바생 대시보드 — 소속 매장 목록. 카드 클릭 시 근무 가능 시간 등록으로 이동. */
export function PartTimerDashboard() {
  const groups = useMyGroupsQuery()

  return (
    <div className="px-10 pb-12 pt-8">
      <ReceivedInvitations />

      <div className="mb-6">
        <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">マイグループ</h1>
        <p className="mt-1.5 text-sm text-muted-foreground">所属店舗で勤務可能時間を登録しましょう</p>
      </div>

      {groups.isPending ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {[0, 1].map((i) => (
            <div key={i} className="h-[160px] animate-pulse rounded-2xl border border-border bg-secondary/50" />
          ))}
        </div>
      ) : groups.isError ? (
        <Centered icon={<IconWarn size={28} />} title="読み込めませんでした" text={groups.error.message} />
      ) : groups.data.length === 0 ? (
        <Centered
          icon={<IconUsers size={28} />}
          title="まだ所属している店舗がありません"
          text="オーナーが招待するとここに店舗が表示されます。"
        />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {groups.data.map((g, i) => (
            <GroupCard key={g.id} group={g} tone={CARD_TONES[i % CARD_TONES.length]} />
          ))}
        </div>
      )}
    </div>
  )
}

function GroupCard({ group, tone }: { group: MembershipGroup; tone: { bg: string; fg: string } }) {
  return (
    <Link
      to={`/groups/${group.id}/availability`}
      className="rounded-2xl border border-border bg-card p-5 transition-colors hover:border-[#E1E4E8]"
    >
      <div
        className="mb-3.5 grid h-11 w-11 place-items-center rounded-xl text-lg font-extrabold"
        style={{ background: tone.bg, color: tone.fg }}
      >
        {group.name.slice(0, 1)}
      </div>
      <div className="text-base font-extrabold">{group.name}</div>
      <p className="mt-1 line-clamp-2 min-h-[32px] text-xs text-muted-foreground">
        {group.description || '説明がありません'}
      </p>
      <div className="my-4 h-px bg-border" />
      <div className="flex items-center gap-1.5 text-xs font-semibold text-primary">
        <IconClock size={14} /> 勤務可能時間を登録する
      </div>
    </Link>
  )
}

function Centered({ icon, title, text }: { icon: React.ReactNode; title: string; text: string }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-border bg-card py-20 text-center">
      <span className="mb-4 grid h-14 w-14 place-items-center rounded-2xl bg-[#EEF3FF] text-primary">{icon}</span>
      <div className="text-lg font-extrabold">{title}</div>
      <p className="mt-1.5 text-sm text-muted-foreground">{text}</p>
    </div>
  )
}
