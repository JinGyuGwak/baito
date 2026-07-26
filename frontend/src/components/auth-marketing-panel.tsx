import { BrandMark, Chip } from '@/components/brand'
import { IconCheck } from '@/components/icons'

/** 인증 화면(로그인/회원가입) 좌측 그라데이션 마케팅 패널. 데스크톱 전용. */
export function AuthMarketingPanel() {
  return (
    <aside className="hidden flex-col justify-between bg-[linear-gradient(160deg,#EEF3FF_0%,#F7F8FA_100%)] p-[72px_60px] lg:flex">
      <BrandMark size={28} />

      <div>
        <h1 className="text-[34px] font-extrabold leading-[1.25] tracking-[-0.025em] text-foreground">
          알바 스케줄,
          <br />
          이제 드래그 한 번이면 끝.
        </h1>
        <p className="mt-4 text-[15px] leading-[1.6] text-muted-foreground">
          점주는 필요한 인원을, 아르바이트생은 가능한 시간을.
          <br />
          시간표가 자동으로 맞춰드려요.
        </p>
        <div className="mt-9 flex gap-2.5">
          <Chip>
            <IconCheck size={12} stroke={2.5} />
            30분 단위 스케줄
          </Chip>
          <Chip>
            <IconCheck size={12} stroke={2.5} />
            자동 충돌 감지
          </Chip>
        </div>
      </div>

      <div className="text-xs text-muted-foreground/70">© 2026 Shijan</div>
    </aside>
  )
}
