import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Spinner } from '@/components/spinner'
import { IconChevL, IconChevR, IconMail, IconWarn } from '@/components/icons'
import { ErrorCode } from '@/types/api'
import { useGroupsQuery } from '@/features/groups'
import {
  useSentInvitationsQuery,
  useCreateInvitationMutation,
  useCancelInvitationMutation,
  type InvitationStatus,
  type SentInvitation,
} from '@/features/invitations'
import { ScheduleTabs } from '@/features/schedule/components/ScheduleTabs'
import { toISODate } from '@/features/schedule/lib/date'

const STATUS_META: Record<InvitationStatus, { label: string; cls: string; dot: string }> = {
  PENDING: { label: '保留中', cls: 'bg-[#FFF6E5] text-[#B0750A]', dot: '#F59E0B' },
  ACCEPTED: { label: '承認済み', cls: 'bg-[#E7F8F1] text-[#047857]', dot: '#10B981' },
  REJECTED: { label: '拒否済み', cls: 'bg-secondary text-muted-foreground', dot: '#B0B8C1' },
  CANCELLED: { label: 'キャンセル済み', cls: 'bg-secondary text-muted-foreground', dot: '#B0B8C1' },
}

type StatusFilter = InvitationStatus | 'ALL'

const FILTERS: { key: StatusFilter; label: string }[] = [
  { key: 'ALL', label: 'すべて' },
  { key: 'PENDING', label: '保留' },
  { key: 'ACCEPTED', label: '承認' },
  { key: 'REJECTED', label: '拒否' },
  { key: 'CANCELLED', label: 'キャンセル' },
]

const PAGE_SIZE = 10

/** 점주 알바생 초대 관리 — loginId로 초대하고 보낸 초대 목록(이름/상태/페이징)을 관리. */
export function InvitationsPage() {
  const { groupId: groupIdParam } = useParams()
  const groupId = Number(groupIdParam)
  const today = toISODate(new Date())

  const groups = useGroupsQuery()
  const groupName = groups.data?.find((g) => g.id === groupId)?.name ?? '店舗'

  const [filter, setFilter] = useState<StatusFilter>('ALL')
  const [page, setPage] = useState(0)

  const sent = useSentInvitationsQuery({
    groupId,
    status: filter === 'ALL' ? undefined : filter,
    page,
    size: PAGE_SIZE,
  })

  const changeFilter = (key: StatusFilter) => {
    setFilter(key)
    setPage(0)
  }

  if (!Number.isFinite(groupId)) {
    return (
      <div className="px-8 pt-6">
        <CenteredMessage title="不正なアクセス" text="グループが見つかりません。" />
      </div>
    )
  }

  const items = sent.data?.content ?? []
  const totalPages = sent.data?.totalPages ?? 0
  const totalElements = sent.data?.totalElements ?? 0

  return (
    <div className="px-8 pb-10 pt-6">
      <ScheduleTabs groupId={groupId} date={today} active="invitations" />

      <div className="mb-6">
        <div className="mb-1.5 flex items-center gap-2 text-xs text-muted-foreground">
          <span>{groupName}</span>
          <IconChevR size={12} />
          <span className="font-bold text-foreground">アルバイト招待管理</span>
        </div>
        <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">アルバイト招待管理</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          アルバイトのIDに招待を送ると、相手が承認したときにグループに参加します
        </p>
      </div>

      <div className="grid grid-cols-1 items-start gap-4 lg:grid-cols-[1fr_320px]">
        {/* 보낸 초대 목록 */}
        <div className="rounded-2xl border border-border bg-card">
          <div className="flex items-center justify-between border-b border-border px-5 py-3.5">
            <div className="text-xs font-bold uppercase tracking-[0.04em] text-muted-foreground">
              送信した招待{totalElements > 0 && ` · ${totalElements}`}
            </div>
            {/* 상태 필터 */}
            <div className="flex gap-1">
              {FILTERS.map((f) => (
                <button
                  key={f.key}
                  type="button"
                  onClick={() => changeFilter(f.key)}
                  className={[
                    'rounded-full px-2.5 py-1 text-xs font-bold',
                    filter === f.key
                      ? 'bg-foreground text-white'
                      : 'text-muted-foreground hover:bg-secondary',
                  ].join(' ')}
                >
                  {f.label}
                </button>
              ))}
            </div>
          </div>

          {sent.isPending ? (
            <div className="flex flex-col gap-2 p-5">
              <div className="h-16 animate-pulse rounded-xl bg-secondary/50" />
              <div className="h-16 animate-pulse rounded-xl bg-secondary/50" />
            </div>
          ) : sent.isError ? (
            <div className="p-8">
              <CenteredMessage title="読み込めませんでした" text={sent.error.message} />
            </div>
          ) : items.length === 0 ? (
            <div className="px-5 py-14 text-center text-sm text-muted-foreground">
              {filter === 'ALL'
                ? 'まだ送信した招待がありません。右側からアルバイトを招待してみましょう。'
                : 'この状態の招待はありません。'}
            </div>
          ) : (
            <>
              {items.map((inv, i) => (
                <InvitationRow key={inv.id} invitation={inv} isLast={i === items.length - 1} />
              ))}
              {totalPages > 1 && (
                <Pagination page={page} totalPages={totalPages} onChange={setPage} />
              )}
            </>
          )}
        </div>

        {/* 초대 보내기 */}
        <InviteForm groupId={groupId} />
      </div>
    </div>
  )
}

function InvitationRow({ invitation, isLast }: { invitation: SentInvitation; isLast: boolean }) {
  const cancel = useCancelInvitationMutation()
  const meta = STATUS_META[invitation.status]
  const displayName = invitation.inviteeName ?? `会員 #${invitation.inviteeId}`
  const loginId = invitation.inviteeLoginId

  return (
    <div className={`flex items-center gap-3 px-5 py-4 ${isLast ? '' : 'border-b border-border'}`}>
      <span
        className="grid h-9 w-9 place-items-center rounded-full text-xs font-bold text-white"
        style={{ background: `hsl(${(invitation.inviteeId * 73) % 360} 60% 62%)` }}
      >
        {displayName.slice(0, 1)}
      </span>
      <div className="flex-1">
        <div className="text-sm font-bold">
          {displayName}
          {loginId && <span className="ml-1 font-medium text-muted-foreground">({loginId})</span>}
        </div>
        <div className="text-xs text-muted-foreground">{formatDateTime(invitation.createdAt)}</div>
      </div>
      <span className={`inline-flex h-[26px] items-center gap-1.5 rounded-full px-2.5 text-xs font-semibold ${meta.cls}`}>
        <span className="h-1.5 w-1.5 rounded-full" style={{ background: meta.dot }} />
        {meta.label}
      </span>
      {invitation.status === 'PENDING' && (
        <Button
          variant="ghost"
          onClick={() => cancel.mutate(invitation.id)}
          disabled={cancel.isPending}
          className="h-8 px-2.5 text-[13px] font-semibold text-[#F04452] hover:text-[#F04452]"
        >
          {cancel.isPending ? <Spinner size={14} /> : 'キャンセル'}
        </Button>
      )}
    </div>
  )
}

function Pagination({
  page,
  totalPages,
  onChange,
}: {
  page: number
  totalPages: number
  onChange: (page: number) => void
}) {
  return (
    <div className="flex items-center justify-center gap-2 border-t border-border px-5 py-3">
      <button
        type="button"
        onClick={() => onChange(page - 1)}
        disabled={page === 0}
        className="grid h-8 w-8 place-items-center rounded-lg text-muted-foreground hover:bg-secondary disabled:opacity-40"
        aria-label="前のページ"
      >
        <IconChevL size={16} />
      </button>
      <span className="text-[13px] font-bold">
        {page + 1} <span className="font-medium text-muted-foreground">/ {totalPages}</span>
      </span>
      <button
        type="button"
        onClick={() => onChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="grid h-8 w-8 place-items-center rounded-lg text-muted-foreground hover:bg-secondary disabled:opacity-40"
        aria-label="次のページ"
      >
        <IconChevR size={16} />
      </button>
    </div>
  )
}

function InviteForm({ groupId }: { groupId: number }) {
  const create = useCreateInvitationMutation()
  const [loginId, setLoginId] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const value = loginId.trim()
    if (value.length === 0 || create.isPending) return
    create.mutate(
      { groupId, inviteeLoginId: value },
      { onSuccess: () => setLoginId('') },
    )
  }

  return (
    <div className="rounded-2xl border border-border bg-card p-5">
      <div className="mb-1.5 flex items-center gap-2.5">
        <span className="grid h-8 w-8 place-items-center rounded-[10px] bg-[#EEF3FF] text-primary">
          <IconMail size={16} />
        </span>
        <div className="text-[15px] font-extrabold">アルバイトを招待</div>
      </div>
      <p className="mb-4 text-[13px] text-muted-foreground">招待するアルバイトのログインIDを入力してください。</p>

      <form onSubmit={handleSubmit} className="flex flex-col gap-2">
        <Label htmlFor="inviteeLoginId" className="text-[13px] font-semibold text-muted-foreground">
          アルバイトのログインID
        </Label>
        <Input
          id="inviteeLoginId"
          placeholder="worker01"
          value={loginId}
          onChange={(e) => setLoginId(e.target.value)}
          autoComplete="off"
        />

        {create.isError && (
          <div className="mt-1 flex items-start gap-2 rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]">
            <span className="mt-0.5">
              <IconWarn size={16} stroke={2} />
            </span>
            {toInviteMessage(create.error.code, create.error.message)}
          </div>
        )}
        {create.isSuccess && (
          <div className="mt-1 rounded-xl bg-[hsl(var(--success-bg))] px-3.5 py-3 text-[13px] font-medium text-[hsl(var(--success))]">
            招待を送信しました。
          </div>
        )}

        <Button type="submit" disabled={loginId.trim().length === 0 || create.isPending} className="mt-2 w-full font-bold">
          {create.isPending ? (
            <>
              <Spinner /> 送信中…
            </>
          ) : (
            '招待を送信'
          )}
        </Button>
      </form>

      <div className="my-4 h-px bg-border" />
      <p className="text-xs leading-relaxed text-muted-foreground">
        アルバイトが招待を承認すると、このグループに自動的に参加します。承認までは <b className="text-foreground">保留中</b> の状態で表示されます。
      </p>
    </div>
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

/** 초대 생성 에러 코드 → 사용자 메시지. */
function toInviteMessage(code: string, fallback: string): string {
  switch (code) {
    case ErrorCode.MEMBER_NOT_FOUND:
      return 'このIDの会員が見つかりません。'
    default:
      return fallback
  }
}

function formatDateTime(iso: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '—'
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
