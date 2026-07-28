import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Spinner } from '@/components/spinner'
import { Avatar } from '@/components/layout/AppBar'
import { IconCheck, IconWarn } from '@/components/icons'
import { useMyProfileQuery, useUpdateNameMutation } from '@/features/member'

/** 내 프로필 — 아이디/역할은 읽기 전용, 이름은 수정 가능. */
export function ProfilePage() {
  const profile = useMyProfileQuery()
  const update = useUpdateNameMutation()

  const [name, setName] = useState('')

  // 프로필 로드/갱신 시 입력값 동기화.
  useEffect(() => {
    if (profile.data) setName(profile.data.name)
  }, [profile.data])

  const trimmed = name.trim()
  const dirty = profile.data ? trimmed !== profile.data.name : false
  const canSave = dirty && trimmed.length > 0 && !update.isPending

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!canSave) return
    update.mutate({ name: trimmed })
  }

  return (
    <div className="px-8 pb-10 pt-6">
      <div className="mb-6">
        <h1 className="text-[26px] font-extrabold tracking-[-0.02em]">프로필</h1>
        <p className="mt-1 text-sm text-muted-foreground">내 계정 정보를 확인하고 이름을 수정할 수 있어요.</p>
      </div>

      {profile.isPending ? (
        <div className="h-64 max-w-lg animate-pulse rounded-2xl border border-border bg-secondary/40" />
      ) : profile.isError ? (
        <div className="flex max-w-lg flex-col items-center justify-center rounded-2xl border border-border bg-card py-14 text-center">
          <span className="mb-3 grid h-12 w-12 place-items-center rounded-2xl bg-[#FFECEE] text-[#F04452]">
            <IconWarn size={28} />
          </span>
          <div className="font-extrabold">불러오지 못했어요</div>
          <p className="mt-1 text-sm text-muted-foreground">{profile.error.message}</p>
        </div>
      ) : (
        <div className="max-w-lg rounded-2xl border border-border bg-card p-6">
          {/* 아바타 + 요약 */}
          <div className="mb-6 flex items-center gap-4">
            <Avatar name={profile.data.name} size={56} />
            <div>
              <div className="text-lg font-extrabold">{profile.data.name}</div>
              <div className="text-sm text-muted-foreground">
                {profile.data.role === 'OWNER' ? '점주' : '아르바이트생'}
              </div>
            </div>
          </div>

          <div className="mb-5 flex flex-col gap-1.5">
            <Label className="text-[13px] font-semibold text-muted-foreground">아이디</Label>
            <div className="rounded-xl bg-[hsl(var(--field))] px-3.5 py-2.5 text-sm font-bold text-muted-foreground">
              {profile.data.loginId}
              <span className="ml-2 text-xs font-medium">변경할 수 없어요</span>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="flex flex-col gap-1.5">
            <Label htmlFor="name" className="text-[13px] font-semibold text-muted-foreground">
              이름
            </Label>
            <Input
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              maxLength={50}
              placeholder="이름을 입력하세요"
              autoComplete="off"
            />

            {update.isError && (
              <div className="mt-1 flex items-start gap-2 rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]">
                <span className="mt-0.5">
                  <IconWarn size={16} stroke={2} />
                </span>
                {update.error.message}
              </div>
            )}
            {update.isSuccess && !dirty && (
              <div className="mt-1 flex items-center gap-1.5 rounded-xl bg-[hsl(var(--success-bg))] px-3.5 py-3 text-[13px] font-medium text-[hsl(var(--success))]">
                <IconCheck size={15} stroke={2.5} /> 이름을 변경했어요.
              </div>
            )}

            <Button type="submit" disabled={!canSave} className="mt-3 font-bold">
              {update.isPending ? (
                <>
                  <Spinner /> 저장 중…
                </>
              ) : (
                '이름 저장'
              )}
            </Button>
          </form>
        </div>
      )}
    </div>
  )
}
