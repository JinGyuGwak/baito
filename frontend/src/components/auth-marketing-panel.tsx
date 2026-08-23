import { BrandMark, Chip } from '@/components/brand'
import { IconCheck } from '@/components/icons'

/** 인증 화면(로그인/회원가입) 좌측 그라데이션 마케팅 패널. 데스크톱 전용. */
export function AuthMarketingPanel() {
  return (
    <aside className="hidden flex-col justify-between bg-[linear-gradient(160deg,#EEF3FF_0%,#F7F8FA_100%)] p-[72px_60px] lg:flex">
      <BrandMark size={28} />

      <div>
        <h1 className="text-[34px] font-extrabold leading-[1.25] tracking-[-0.025em] text-foreground">
          アルバイトのシフトは、
          <br />
          ドラッグ一つで完了。
        </h1>
        <p className="mt-4 text-[15px] leading-[1.6] text-muted-foreground">
          オーナーは必要な人数を、アルバイトは勤務可能な時間を。
          <br />
          シフト表が自動で調整します。
        </p>
        <div className="mt-9 flex gap-2.5">
          <Chip>
            <IconCheck size={12} stroke={2.5} />
            30分単位のシフト
          </Chip>
          <Chip>
            <IconCheck size={12} stroke={2.5} />
            自動の重複検知
          </Chip>
        </div>
      </div>

      <div className="text-xs text-muted-foreground/70">© 2026 Shijan</div>
    </aside>
  )
}
