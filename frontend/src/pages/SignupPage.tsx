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
          <h1 className="text-[28px] font-extrabold tracking-[-0.025em]">どの役割で始めますか？</h1>
          <p className="mt-2.5 text-muted-foreground">登録後でも役割を追加で登録できます。</p>
        </div>

        <div className="mt-9 grid gap-4 sm:grid-cols-2">
          <RoleCard
            selected={role === 'OWNER'}
            onSelect={() => onChange('OWNER')}
            icon={<IconStore size={28} />}
            accentBg="bg-[#EEF3FF]"
            accentFg="text-primary"
            title="オーナー"
            desc="店舗を運営し、アルバイトのシフトを自分で組みます。"
            features={['複数のグループ（店舗）を運営', '30分単位の人数設定', 'シフト割り当て管理']}
          />
          <RoleCard
            selected={role === 'PART_TIMER'}
            onSelect={() => onChange('PART_TIMER')}
            icon={<IconUsers size={28} />}
            accentBg="bg-[#FFF3EC]"
            accentFg="text-[#E66A2C]"
            title="アルバイト"
            desc="複数の店舗に所属し、勤務可能な時間を伝えるだけです。"
            features={['日ごとの勤務可能時間を登録', '割り当て通知の確認', '複数店舗を同時に管理']}
          />
        </div>

        <Button
          onClick={onContinue}
          className="mt-6 h-[52px] w-full rounded-[14px] text-[15px] font-bold"
        >
          {role === 'OWNER' ? 'オーナー' : 'アルバイト'}で始める
          <IconArrow size={16} stroke={2} />
        </Button>

        <p className="mt-4 text-center text-sm text-muted-foreground">
          すでにアカウントをお持ちですか？{' '}
          <Link to="/login" className="font-bold text-primary hover:underline">
            ログイン
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
    name: z.string().min(1, '名前を入力してください').max(50, '名前は最大50文字です'),
    loginId: z.string().min(1, 'IDを入力してください').max(50, 'IDは最大50文字です'),
    password: z.string().min(8, 'パスワードは8文字以上で入力してください').max(64, 'パスワードは最大64文字です'),
    passwordConfirm: z.string().min(1, 'パスワードをもう一度入力してください'),
  })
  .refine((d) => d.password === d.passwordConfirm, {
    path: ['passwordConfirm'],
    message: 'パスワードが一致しません',
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

          <h2 className="text-2xl font-extrabold tracking-[-0.02em]">新規登録</h2>
          <p className="mt-1.5 text-sm text-muted-foreground">
            すでにアカウントをお持ちですか？{' '}
            <Link to="/login" className="font-bold text-primary hover:underline">
              ログイン
            </Link>
          </p>

          {/* 선택한 역할 표시 + 변경 */}
          <div className="mt-5 flex items-center justify-between rounded-xl bg-[hsl(var(--field))] px-4 py-3">
            <span className="text-sm">
              <span className="text-muted-foreground">選択した役割 · </span>
              <span className="font-bold">{role === 'OWNER' ? 'オーナー' : 'アルバイト'}</span>
            </span>
            <button
              type="button"
              onClick={onBack}
              className="text-[13px] font-semibold text-primary hover:underline"
            >
              役割を変更
            </button>
          </div>

          <form onSubmit={onSubmit} className="mt-6 flex flex-col gap-3.5" noValidate>
            <Field label="名前" htmlFor="name" error={errors.name?.message}>
              <Input id="name" placeholder="山田太郎" autoComplete="name" {...register('name')} />
            </Field>

            <Field label="ID" htmlFor="loginId" error={errors.loginId?.message}>
              <Input id="loginId" placeholder="owner01" autoComplete="username" {...register('loginId')} />
            </Field>

            <Field label="パスワード" htmlFor="password" error={errors.password?.message}>
              <Input
                id="password"
                type="password"
                placeholder="8〜64文字"
                autoComplete="new-password"
                {...register('password')}
              />
            </Field>

            <Field label="パスワード（確認）" htmlFor="passwordConfirm" error={errors.passwordConfirm?.message}>
              <Input
                id="passwordConfirm"
                type="password"
                placeholder="パスワードをもう一度入力してください"
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
                  登録中…
                </>
              ) : (
                <>
                  登録して始める
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
  if (code === ErrorCode.DUPLICATE_LOGIN_ID) return 'すでに使用されているIDです。'
  return fallback
}
