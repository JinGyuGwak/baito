import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/spinner'
import { IconCheck, IconWarn, IconX } from '@/components/icons'
import {
  useReceivedInvitationsQuery,
  useAcceptInvitationMutation,
  useRejectInvitationMutation,
  type Invitation,
} from '@/features/invitations'

/**
 * 알바생이 받은(PENDING) 초대 목록. 수락 시 그룹에 합류.
 * 초대가 없으면 아무것도 렌더하지 않는다(대시보드에서 빈 섹션 방지).
 */
export function ReceivedInvitations() {
  const received = useReceivedInvitationsQuery()

  if (received.isPending || received.isError) return null
  if (received.data.length === 0) return null

  return (
    <section className="mb-8">
      <h2 className="mb-3 text-[13px] font-extrabold uppercase tracking-[0.04em] text-muted-foreground">
        받은 초대 · {received.data.length}
      </h2>
      <div className="flex flex-col gap-3">
        {received.data.map((inv) => (
          <InvitationCard key={inv.id} invitation={inv} />
        ))}
      </div>
    </section>
  )
}

function InvitationCard({ invitation }: { invitation: Invitation }) {
  const accept = useAcceptInvitationMutation()
  const reject = useRejectInvitationMutation()
  const busy = accept.isPending || reject.isPending
  const error = accept.error ?? reject.error

  return (
    <div className="overflow-hidden rounded-2xl border border-border bg-card">
      <div className="h-1.5 bg-[linear-gradient(90deg,#3D6AFF,#6E8BFF)]" />
      <div className="p-5">
        <div className="flex gap-4">
          <span className="grid h-14 w-14 flex-shrink-0 place-items-center rounded-2xl bg-[#EEF3FF] text-lg font-extrabold text-primary">
            #{invitation.groupId}
          </span>
          <div className="flex-1">
            <div className="text-[17px] font-extrabold">그룹 #{invitation.groupId}</div>
            <div className="mt-1 text-[13px] text-muted-foreground">
              초대한 점주 #{invitation.inviterId} · {formatRelative(invitation.createdAt)}
            </div>
          </div>
        </div>

        {error && (
          <div className="mt-3 flex items-center gap-2 rounded-xl bg-[#FFECEE] px-3.5 py-2.5 text-[13px] font-medium text-[#F04452]">
            <IconWarn size={16} stroke={2} /> {error.message}
          </div>
        )}

        <div className="mt-4 flex gap-2">
          <Button
            variant="secondary"
            onClick={() => reject.mutate(invitation.id)}
            disabled={busy}
            className="flex-1 font-bold text-muted-foreground"
          >
            {reject.isPending ? <Spinner size={14} /> : <IconX size={14} />} 거절
          </Button>
          <Button
            onClick={() => accept.mutate(invitation.id)}
            disabled={busy}
            className="flex-[2] font-bold"
          >
            {accept.isPending ? <Spinner size={14} /> : <IconCheck size={16} stroke={2.5} />} 수락하고 가입
          </Button>
        </div>
      </div>
    </div>
  )
}

/** ISO → "N분 전 / N시간 전 / N일 전". */
function formatRelative(iso: string): string {
  const then = new Date(iso).getTime()
  if (Number.isNaN(then)) return ''
  const diff = Date.now() - then
  const min = Math.floor(diff / 60000)
  if (min < 1) return '방금'
  if (min < 60) return `${min}분 전`
  const hr = Math.floor(min / 60)
  if (hr < 24) return `${hr}시간 전`
  return `${Math.floor(hr / 24)}일 전`
}
