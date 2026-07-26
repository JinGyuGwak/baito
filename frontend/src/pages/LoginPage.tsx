import { useState } from 'react'
import { Navigate, Link, useLocation, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { IconArrow, IconCheck, IconWarn } from '@/components/icons'
import { BrandMark } from '@/components/brand'
import { AuthMarketingPanel } from '@/components/auth-marketing-panel'
import { Spinner } from '@/components/spinner'
import { useAuthStore, useLoginMutation } from '@/features/auth'

/**
 * 로그인 화면.
 *
 * - 디자인: 시간표(Shijan) 온보딩 패턴 — 좌측 그라데이션 마케팅 패널 + 우측 폼.
 * - 폼 상태: 필드 2개뿐이라 react-hook-form 없이 useState 로 관리.
 *   (필드 많은 회원가입/스케줄 폼부터 RHF+zod 도입 예정)
 * - API: features/auth 의 useLoginMutation. 백엔드 로그인은 loginId + password.
 */
export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const token = useAuthStore((s) => s.token)
  const login = useLoginMutation()

  // 회원가입 직후 진입 시: 성공 배너 + 아이디 프리필.
  const signupState = location.state as { justSignedUp?: boolean; loginId?: string } | null
  const [loginId, setLoginId] = useState(signupState?.loginId ?? '')
  const [password, setPassword] = useState('')
  const [showSignupDone, setShowSignupDone] = useState(Boolean(signupState?.justSignedUp))

  // 이미 로그인된 상태로 /login 진입 시 홈으로.
  if (token) return <Navigate to="/" replace />

  const canSubmit = loginId.trim().length > 0 && password.length > 0 && !login.isPending

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!canSubmit) return
    setShowSignupDone(false)
    login.mutate(
      { loginId: loginId.trim(), password },
      { onSuccess: () => navigate('/', { replace: true }) },
    )
  }

  return (
    <div className="grid min-h-screen bg-[hsl(var(--bg-soft))] lg:grid-cols-2">
      {/* ── 좌측 마케팅 패널 (데스크톱 전용) ── */}
      <AuthMarketingPanel />

      {/* ── 우측 로그인 폼 ── */}
      <main className="flex flex-col justify-center bg-background px-6 py-16 sm:px-20">
        <div className="mx-auto w-full max-w-[380px]">
          {/* 모바일에서만 보이는 브랜드 */}
          <div className="mb-8 lg:hidden">
            <BrandMark size={24} />
          </div>

          <h2 className="text-2xl font-extrabold tracking-[-0.02em]">로그인</h2>
          <p className="mt-1.5 text-sm text-muted-foreground">
            아직 계정이 없나요?{' '}
            <Link to="/signup" className="font-bold text-primary hover:underline">
              회원가입
            </Link>
          </p>

          {showSignupDone && (
            <div
              role="status"
              className="mt-6 flex items-center gap-2 rounded-xl bg-[hsl(var(--success-bg))] px-3.5 py-3 text-[13px] font-medium text-[hsl(var(--success))]"
            >
              <IconCheck size={16} stroke={2.5} />
              가입이 완료됐어요. 로그인해 주세요.
            </div>
          )}

          <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-3.5">
            <div className="flex flex-col gap-2">
              <Label htmlFor="loginId" className="text-[13px] font-semibold text-[hsl(var(--muted-foreground))]">
                아이디
              </Label>
              <Input
                id="loginId"
                autoComplete="username"
                placeholder="owner01"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                disabled={login.isPending}
                autoFocus
              />
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="password" className="text-[13px] font-semibold text-[hsl(var(--muted-foreground))]">
                비밀번호
              </Label>
              <Input
                id="password"
                type="password"
                autoComplete="current-password"
                placeholder="비밀번호를 입력하세요"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={login.isPending}
              />
            </div>

            {login.isError && (
              <div
                role="alert"
                className="flex items-center gap-2 rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]"
              >
                <IconWarn size={16} stroke={2} />
                {login.error.message}
              </div>
            )}

            <Button
              type="submit"
              disabled={!canSubmit}
              className="mt-3 h-[52px] w-full rounded-[14px] text-[15px] font-bold"
            >
              {login.isPending ? (
                <>
                  <Spinner />
                  로그인 중…
                </>
              ) : (
                <>
                  로그인
                  <IconArrow size={16} stroke={2} />
                </>
              )}
            </Button>
          </form>

          {/* 소셜 로그인 — 디자인 유지용. 백엔드 미지원이라 비활성 처리. */}
          <div className="my-6 flex items-center gap-2.5 text-xs text-muted-foreground">
            <span className="h-px flex-1 bg-border" />
            또는
            <span className="h-px flex-1 bg-border" />
          </div>
          <div className="flex gap-2">
            <Button
              type="button"
              disabled
              title="준비 중"
              className="h-11 flex-1 rounded-xl bg-[#FEE500] font-bold text-[#191F28] hover:bg-[#FEE500]/90"
            >
              카카오로 시작
            </Button>
            <Button
              type="button"
              variant="secondary"
              disabled
              title="준비 중"
              className="h-11 flex-1 rounded-xl font-bold"
            >
              Google로 시작
            </Button>
          </div>
        </div>
      </main>
    </div>
  )
}

