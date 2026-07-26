import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { IconPlus, IconStore, IconWarn } from '@/components/icons'
import { useAuthStore } from '@/features/auth'
import { useGroupsQuery, CreateGroupDialog, type Group } from '@/features/groups'
import { useRequiredStaffQuery } from '@/features/schedule'
import { toISODate } from '@/features/schedule/lib/date'
import { useAssignmentsQuery, computeCoverage } from '@/features/assignments'
import { PartTimerDashboard } from './PartTimerDashboard'

const CARD_TONES = [
  { bg: '#EEF3FF', fg: '#3D6AFF' },
  { bg: '#FFF3EC', fg: '#E66A2C' },
  { bg: '#E7F8F1', fg: '#10B981' },
  { bg: '#F2EDFE', fg: '#8B5CF6' },
  { bg: '#FDE7F2', fg: '#EC4899' },
]

/** 대시보드 — 역할에 따라 점주(소유 그룹) / 알바생(소속 그룹) 화면으로 분기. */
export function DashboardPage() {
  const role = useAuthStore((s) => s.user?.role)
  if (role === 'PART_TIMER') return <PartTimerDashboard />
  return <OwnerDashboard />
}

/** 점주 대시보드 — 소유한 그룹(매장) 목록. */
function OwnerDashboard() {
  const groups = useGroupsQuery()
  const [searchParams, setSearchParams] = useSearchParams()
  const [createOpen, setCreateOpen] = useState(false)

  // 사이드바의 "새 그룹 만들기"(/?create=1) 진입 시 자동으로 다이얼로그 오픈.
  useEffect(() => {
    if (searchParams.get('create') === '1') {
      setCreateOpen(true)
      searchParams.delete('create')
      setSearchParams(searchParams, { replace: true })
    }
  }, [searchParams, setSearchParams])

  return (
    <div className="px-10 pb-12 pt-8">
      <div className="mb-6 flex items-end justify-between">
        <div>
          <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">내 그룹</h1>
          <p className="mt-1.5 text-sm text-muted-foreground">운영중인 매장(그룹)을 한 곳에서 관리하세요</p>
        </div>
        <Button onClick={() => setCreateOpen(true)} className="font-bold">
          <IconPlus size={16} stroke={2.5} /> 새 그룹 만들기
        </Button>
      </div>

      {groups.isPending ? (
        <LoadingState />
      ) : groups.isError ? (
        <ErrorState message={groups.error.message} onRetry={() => groups.refetch()} />
      ) : groups.data.length === 0 ? (
        <EmptyState onCreate={() => setCreateOpen(true)} />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {groups.data.map((g, i) => (
            <GroupCard key={g.id} group={g} tone={CARD_TONES[i % CARD_TONES.length]} />
          ))}
          <AddGroupCard onClick={() => setCreateOpen(true)} />
        </div>
      )}

      <CreateGroupDialog open={createOpen} onOpenChange={setCreateOpen} />
    </div>
  )
}

function GroupCard({ group, tone }: { group: Group; tone: { bg: string; fg: string } }) {
  return (
    <Link
      to={`/groups/${group.id}/schedule`}
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
        {group.description || '설명이 없어요'}
      </p>
      <div className="my-4 h-px bg-border" />
      <div className="flex items-center justify-between text-xs text-muted-foreground">
        <span>오늘 현황</span>
        <GroupTodayStatus groupId={group.id} />
      </div>
    </Link>
  )
}

/** 그룹 카드의 오늘 인원 충원 현황 칩 — 오늘 배정/필요를 계산해 표시. */
function GroupTodayStatus({ groupId }: { groupId: number }) {
  const today = toISODate(new Date())
  const required = useRequiredStaffQuery(groupId, today)
  const assignments = useAssignmentsQuery(groupId, today)

  if (required.isPending || assignments.isPending) {
    return <span className="h-5 w-16 animate-pulse rounded-full bg-secondary" />
  }
  if (required.isError || assignments.isError) {
    return <span className="font-bold text-muted-foreground">—</span>
  }

  const { totalNeeded, totalFilled, shortSlots } = computeCoverage(required.data, assignments.data)
  if (totalNeeded === 0) {
    return <span className="font-semibold text-muted-foreground">필요 인원 미설정</span>
  }

  const short = shortSlots > 0
  return (
    <span
      className={`inline-flex h-[22px] items-center gap-1.5 rounded-full px-2 text-[11px] font-bold ${
        short ? 'bg-[#FFF6E5] text-[#B0750A]' : 'bg-[#E7F8F1] text-[#047857]'
      }`}
    >
      <span className="h-1.5 w-1.5 rounded-full" style={{ background: short ? '#F59E0B' : '#10B981' }} />
      채움 {totalFilled}/{totalNeeded}
      {short && ' · 부족'}
    </span>
  )
}

function AddGroupCard({ onClick }: { onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex min-h-[200px] flex-col items-center justify-center rounded-2xl border-[1.5px] border-dashed border-[#D1D6DB] text-muted-foreground hover:border-[#B0B8C1]"
    >
      <span className="mb-2.5 grid h-11 w-11 place-items-center rounded-xl bg-secondary">
        <IconPlus size={20} />
      </span>
      <span className="font-bold text-[#4E5968]">새 그룹 만들기</span>
      <span className="mt-1 text-xs">매장을 추가로 운영중이라면</span>
    </button>
  )
}

function LoadingState() {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
      {[0, 1, 2].map((i) => (
        <div key={i} className="h-[200px] animate-pulse rounded-2xl border border-border bg-secondary/50" />
      ))}
    </div>
  )
}

function EmptyState({ onCreate }: { onCreate: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-border bg-card py-20 text-center">
      <span className="mb-4 grid h-14 w-14 place-items-center rounded-2xl bg-[#EEF3FF] text-primary">
        <IconStore size={28} />
      </span>
      <div className="text-lg font-extrabold">아직 운영중인 그룹이 없어요</div>
      <p className="mt-1.5 text-sm text-muted-foreground">첫 매장을 만들고 스케줄을 짜보세요.</p>
      <Button onClick={onCreate} className="mt-5 font-bold">
        <IconPlus size={16} stroke={2.5} /> 새 그룹 만들기
      </Button>
    </div>
  )
}

function ErrorState({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-border bg-card py-20 text-center">
      <span className="mb-4 grid h-14 w-14 place-items-center rounded-2xl bg-[#FFECEE] text-[#F04452]">
        <IconWarn size={28} />
      </span>
      <div className="text-lg font-extrabold">그룹을 불러오지 못했어요</div>
      <p className="mt-1.5 text-sm text-muted-foreground">{message}</p>
      <Button variant="secondary" onClick={onRetry} className="mt-5 font-bold">
        다시 시도
      </Button>
    </div>
  )
}
