import { useState } from 'react'
import { Navigate, Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { BrandMark } from '@/components/brand'
import { AuthMarketingPanel } from '@/components/auth-marketing-panel'
import { Spinner } from '@/components/spinner'
import { IconArrow, IconCheck, IconStore, IconUsers, IconWarn } from '@/components/icons'
import type { Role } from '@/types/api'
import { ErrorCode } from '@/types/api'
import { useAuthStore } from '@/features/auth'
import { useSignUpMutation } from '@/features/member'

/**
 * 회원가입 — 2단계 플로우.
 *   1) 역할 선택(점주 / 아르바이트생)  2) 계정 정보 입력
 *
 * - 폼 상태: 필드가 많고 검증(길이/일치)이 있어 react-hook-form + zod 사용.
 * - API: POST /api/members ( loginId, password, name, role ). 성공 시 토큰이 없으므로
 *   로그인 화면으로 이동(아이디 프리필). 디자인의 이메일/휴대폰은 백엔드 미지원이라 제외.
 */
export function SignupPage() {
  const token = useAuthStore((s) => s.token)
  const [step, setStep] = useState<'role' | 'form'>('role')
  const [role, setRole] = useState<Role>('OWNER')

  if (token) return <Navigate to="/" replace />

  if (step === 'role') {
    return (
      <RoleSelectStep
        role={role}
        onChange={setRole}
        onContinue={() => setStep('form')}
      />
    )
  }
  return <SignupFormStep role={role} onBack={() => setStep('role')} />
}

/* ───────────────────────── Step 1: 역할 선택 ───────────────────────── */

function RoleSelectStep({
  role,
  onChange,
  onContinue,
}: {
  role: Role
  onChange: (r: Role) => void
  onContinue: () => void
}) {
  return (
    <div className="grid min-h-screen place-items-center bg-[hsl(var(--bg-soft))] px-6 py-12">
      <div className="w-full max-w-[680px]">
        <div className="mb-9 flex justify-center">
          <BrandMark size={22} />
        </div>

        <div className="text-center">
          <h1 className="text-[28px] font-extrabold tracking-[-0.025em]">어떤 역할로 시작할까요?</h1>
          <p className="mt-2.5 text-muted-foreground">가입 후에도 역할을 추가로 등록할 수 있어요.</p>
        </div>

        <div className="mt-9 grid gap-4 sm:grid-cols-2">
          <RoleCard
            selected={role === 'OWNER'}
            onSelect={() => onChange('OWNER')}
            icon={<IconStore size={28} />}
            accentBg="bg-[#EEF3FF]"
            accentFg="text-primary"
            title="점주"
            desc="매장을 운영하며 아르바이트 스케줄을 직접 짭니다."
            features={['그룹(매장) 여러 개 운영', '30분 단위 인원 설정', '근무 배정 관리']}
          />
          <RoleCard
            selected={role === 'PART_TIMER'}
            onSelect={() => onChange('PART_TIMER')}
            icon={<IconUsers size={28} />}
            accentBg="bg-[#FFF3EC]"
            accentFg="text-[#E66A2C]"
            title="아르바이트생"
            desc="여러 매장에 소속되어 가능한 시간만 알려주면 됩니다."
            features={['일자별 가능 시간 등록', '배정 알림 확인', '여러 매장 동시 관리']}
          />
        </div>

        <Button
          onClick={onContinue}
          className="mt-6 h-[52px] w-full rounded-[14px] text-[15px] font-bold"
        >
          {role === 'OWNER' ? '점주' : '아르바이트생'}로 시작하기
          <IconArrow size={16} stroke={2} />
        </Button>

        <p className="mt-4 text-center text-sm text-muted-foreground">
          이미 계정이 있나요?{' '}
          <Link to="/login" className="font-bold text-primary hover:underline">
            로그인
          </Link>
        </p>
      </div>
    </div>
  )
}

function RoleCard({
  selected,
  onSelect,
  icon,
  accentBg,
  accentFg,
  title,
  desc,
  features,
}: {
  selected: boolean
  onSelect: () => void
  icon: React.ReactNode
  accentBg: string
  accentFg: string
  title: string
  desc: string
  features: string[]
}) {
  return (
    <button
      type="button"
      onClick={onSelect}
      aria-pressed={selected}
      className={[
        'relative rounded-2xl border p-7 text-left transition-colors',
        selected
          ? 'border-2 border-primary bg-[linear-gradient(180deg,#fff,#F8FAFF)]'
          : 'border-border bg-card hover:border-[#E1E4E8]',
      ].join(' ')}
    >
      {selected && (
        <span className="absolute right-4 top-4 grid h-[22px] w-[22px] place-items-center rounded-full bg-primary text-white">
          <IconCheck size={14} stroke={2.5} />
        </span>
      )}
      <div className={`mb-[18px] grid h-14 w-14 place-items-center rounded-2xl ${accentBg} ${accentFg}`}>
        {icon}
      </div>
      <div className="text-lg font-extrabold">{title}</div>
      <p className="mt-1.5 text-[13px] leading-[1.6] text-muted-foreground">{desc}</p>
      <div className="mt-[18px] flex flex-col gap-2">
        {features.map((f) => (
          <div key={f} className="flex items-center gap-2 text-[13px] text-foreground/80">
            <span className="text-primary">
              <IconCheck size={14} stroke={2.5} />
            </span>
            {f}
          </div>
        ))}
      </div>
    </button>
  )
}

/* ───────────────────────── Step 2: 계정 정보 ───────────────────────── */

const signupSchema = z
  .object({
    name: z.string().min(1, '이름을 입력하세요').max(50, '이름은 최대 50자입니다'),
    loginId: z.string().min(1, '아이디를 입력하세요').max(50, '아이디는 최대 50자입니다'),
    password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다').max(64, '비밀번호는 최대 64자입니다'),
    passwordConfirm: z.string().min(1, '비밀번호를 한 번 더 입력하세요'),
  })
  .refine((d) => d.password === d.passwordConfirm, {
    path: ['passwordConfirm'],
    message: '비밀번호가 일치하지 않습니다',
  })

type SignupForm = z.infer<typeof signupSchema>

function SignupFormStep({ role, onBack }: { role: Role; onBack: () => void }) {
  const navigate = useNavigate()
  const signUp = useSignUpMutation()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SignupForm>({
    resolver: zodResolver(signupSchema),
    defaultValues: { name: '', loginId: '', password: '', passwordConfirm: '' },
    mode: 'onTouched',
  })

  const onSubmit = handleSubmit((values) => {
    signUp.mutate(
      {
        name: values.name.trim(),
        loginId: values.loginId.trim(),
        password: values.password,
        role,
      },
      {
        onSuccess: () =>
          navigate('/login', {
            replace: true,
            state: { justSignedUp: true, loginId: values.loginId.trim() },
          }),
      },
    )
  })

  const serverError = signUp.isError ? toServerMessage(signUp.error.code, signUp.error.message) : null

  return (
    <div className="grid min-h-screen bg-[hsl(var(--bg-soft))] lg:grid-cols-2">
      <AuthMarketingPanel />

      <main className="flex flex-col justify-center bg-background px-6 py-16 sm:px-20">
        <div className="mx-auto w-full max-w-[380px]">
          <div className="mb-8 lg:hidden">
            <BrandMark size={24} />
          </div>

          <h2 className="text-2xl font-extrabold tracking-[-0.02em]">회원가입</h2>
          <p className="mt-1.5 text-sm text-muted-foreground">
            이미 계정이 있나요?{' '}
            <Link to="/login" className="font-bold text-primary hover:underline">
              로그인
            </Link>
          </p>

          {/* 선택한 역할 표시 + 변경 */}
          <div className="mt-5 flex items-center justify-between rounded-xl bg-[hsl(var(--field))] px-4 py-3">
            <span className="text-sm">
              <span className="text-muted-foreground">선택한 역할 · </span>
              <span className="font-bold">{role === 'OWNER' ? '점주' : '아르바이트생'}</span>
            </span>
            <button
              type="button"
              onClick={onBack}
              className="text-[13px] font-semibold text-primary hover:underline"
            >
              역할 변경
            </button>
          </div>

          <form onSubmit={onSubmit} className="mt-6 flex flex-col gap-3.5" noValidate>
            <Field label="이름" htmlFor="name" error={errors.name?.message}>
              <Input id="name" placeholder="홍길동" autoComplete="name" {...register('name')} />
            </Field>

            <Field label="아이디" htmlFor="loginId" error={errors.loginId?.message}>
              <Input id="loginId" placeholder="owner01" autoComplete="username" {...register('loginId')} />
            </Field>

            <Field label="비밀번호" htmlFor="password" error={errors.password?.message}>
              <Input
                id="password"
                type="password"
                placeholder="8~64자"
                autoComplete="new-password"
                {...register('password')}
              />
            </Field>

            <Field label="비밀번호 확인" htmlFor="passwordConfirm" error={errors.passwordConfirm?.message}>
              <Input
                id="passwordConfirm"
                type="password"
                placeholder="비밀번호를 한 번 더 입력하세요"
                autoComplete="new-password"
                {...register('passwordConfirm')}
              />
            </Field>

            {serverError && (
              <div
                role="alert"
                className="flex items-center gap-2 rounded-xl bg-[#FFECEE] px-3.5 py-3 text-[13px] font-medium text-[#F04452]"
              >
                <IconWarn size={16} stroke={2} />
                {serverError}
              </div>
            )}

            <Button
              type="submit"
              disabled={signUp.isPending}
              className="mt-3 h-[52px] w-full rounded-[14px] text-[15px] font-bold"
            >
              {signUp.isPending ? (
                <>
                  <Spinner />
                  가입 중…
                </>
              ) : (
                <>
                  가입하고 시작하기
                  <IconArrow size={16} stroke={2} />
                </>
              )}
            </Button>
          </form>
        </div>
      </main>
    </div>
  )
}

function Field({
  label,
  htmlFor,
  error,
  children,
}: {
  label: string
  htmlFor: string
  error?: string
  children: React.ReactNode
}) {
  return (
    <div className="flex flex-col gap-2">
      <Label htmlFor={htmlFor} className="text-[13px] font-semibold text-[hsl(var(--muted-foreground))]">
        {label}
      </Label>
      {children}
      {error && <p className="text-[12px] font-medium text-[#F04452]">{error}</p>}
    </div>
  )
}

/** 서버 에러 코드를 사용자 친화 메시지로 변환. */
function toServerMessage(code: string, fallback: string): string {
  if (code === ErrorCode.DUPLICATE_LOGIN_ID) return '이미 사용 중인 아이디입니다.'
  return fallback
}
