import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/spinner'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { IconChevR, IconUsers, IconWarn } from '@/components/icons'
import { useGroupsQuery } from '@/features/groups'
import {
  useGroupMembersQuery,
  useRemoveGroupMemberMutation,
  type GroupMember,
} from '@/features/memberships'
import { ScheduleTabs } from '@/features/schedule/components/ScheduleTabs'
import { toISODate } from '@/features/schedule/lib/date'

/** 점주 알바생 관리 — 그룹에 소속된 알바생 목록을 [이름 (유저ID)]로 보고, 그룹에서 추방. */
export function MembersPage() {
  const { groupId: groupIdParam } = useParams()
  const groupId = Number(groupIdParam)
  const today = toISODate(new Date())

  const groups = useGroupsQuery()
  const groupName = groups.data?.find((g) => g.id === groupId)?.name ?? '매장'

  const members = useGroupMembersQuery(groupId)
  const [target, setTarget] = useState<GroupMember | null>(null)

  if (!Number.isFinite(groupId)) {
    return (
      <div className="px-8 pt-6">
        <CenteredMessage title="잘못된 접근" text="그룹을 찾을 수 없어요." />
      </div>
    )
  }

  const list = members.data ?? []

  return (
    <div className="px-8 pb-10 pt-6">
      <ScheduleTabs groupId={groupId} date={today} active="members" />

      <div className="mb-6">
        <div className="mb-1.5 flex items-center gap-2 text-xs text-muted-foreground">
          <span>{groupName}</span>
          <IconChevR size={12} />
          <span className="font-bold text-foreground">알바생 관리</span>
        </div>
        <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">알바생 관리</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          이 매장에 소속된 알바생 목록이에요. 그룹에서 추방할 수 있어요.
        </p>
      </div>

      <div className="max-w-2xl rounded-2xl border border-border bg-card">
        <div className="flex items-center gap-2 border-b border-border px-5 py-3.5 text-xs font-bold uppercase tracking-[0.04em] text-muted-foreground">
          <IconUsers size={14} />
          소속 알바생{list.length > 0 && ` · ${list.length}`}
        </div>

        {members.isPending ? (
          <div className="flex flex-col gap-2 p-5">
            <div className="h-14 animate-pulse rounded-xl bg-secondary/50" />
            <div className="h-14 animate-pulse rounded-xl bg-secondary/50" />
          </div>
        ) : members.isError ? (
          <div className="p-8">
            <CenteredMessage title="불러오지 못했어요" text={members.error.message} />
          </div>
        ) : list.length === 0 ? (
          <div className="px-5 py-14 text-center text-sm text-muted-foreground">
            아직 소속된 알바생이 없어요. 초대관리 탭에서 알바생을 초대해보세요.
          </div>
        ) : (
          list.map((m, i) => (
            <MemberRow
              key={m.memberId}
              member={m}
              isLast={i === list.length - 1}
              onRemove={() => setTarget(m)}
            />
          ))
        )}
      </div>

      <RemoveMemberDialog groupId={groupId} member={target} onClose={() => setTarget(null)} />
    </div>
  )
}

function MemberRow({
  member,
  isLast,
  onRemove,
}: {
  member: GroupMember
  isLast: boolean
  onRemove: () => void
}) {
  return (
    <div className={`flex items-center gap-3 px-5 py-4 ${isLast ? '' : 'border-b border-border'}`}>
      <span
        className="grid h-9 w-9 place-items-center rounded-full text-xs font-bold text-white"
        style={{ background: `hsl(${(member.memberId * 73) % 360} 60% 62%)` }}
      >
        {member.name.slice(0, 1)}
      </span>
      <div className="flex-1">
        <div className="text-sm font-bold">
          {member.name}
          <span className="ml-1 font-medium text-muted-foreground">({member.loginId})</span>
        </div>
        <div className="text-xs text-muted-foreground">{formatJoinedAt(member.joinedAt)} 합류</div>
      </div>
      <Button
        variant="ghost"
        onClick={onRemove}
        className="h-8 px-2.5 text-[13px] font-semibold text-[#F04452] hover:text-[#F04452]"
      >
        추방
      </Button>
    </div>
  )
}

function RemoveMemberDialog({
  groupId,
  member,
  onClose,
}: {
  groupId: number
  member: GroupMember | null
  onClose: () => void
}) {
  const remove = useRemoveGroupMemberMutation(groupId)

  const handleConfirm = () => {
    if (!member) return
    remove.mutate(member.memberId, {
      onSuccess: () => {
        remove.reset()
        onClose()
      },
    })
  }

  const handleOpenChange = (open: boolean) => {
    if (!open && !remove.isPending) {
      remove.reset()
      onClose()
    }
  }

  return (
    <Dialog open={member != null} onOpenChange={handleOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>알바생 추방</DialogTitle>
          <DialogDescription>
            {member && (
              <>
                <b className="text-foreground">
                  {member.name} ({member.loginId})
                </b>
                님을 그룹에서 추방할까요? 추방하면 이 알바생은 더 이상 근무에 배정되지 않아요.
              </>
            )}
          </DialogDescription>
        </DialogHeader>

        {remove.isError && (
          <div className="flex items-start gap-2 rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]">
            <span className="mt-0.5">
              <IconWarn size={16} stroke={2} />
            </span>
            {remove.error.message}
          </div>
        )}

        <DialogFooter>
          <Button variant="secondary" onClick={onClose} disabled={remove.isPending} className="font-bold">
            취소
          </Button>
          <Button
            onClick={handleConfirm}
            disabled={remove.isPending}
            className="bg-[#F04452] font-bold text-white hover:bg-[#D93A47]"
          >
            {remove.isPending ? (
              <>
                <Spinner /> 추방 중…
              </>
            ) : (
              '추방하기'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

function CenteredMessage({ title, text }: { title: string; text: string }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-border bg-card py-14 text-center">
      <span className="mb-3 grid h-12 w-12 place-items-center rounded-2xl bg-[#FFECEE] text-[#F04452]">
        <IconWarn size={28} />
      </span>
      <div className="font-extrabold">{title}</div>
      <p className="mt-1 text-sm text-muted-foreground">{text}</p>
    </div>
  )
}

function formatJoinedAt(iso: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '—'
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}
