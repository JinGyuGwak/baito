/** 시간표(Shijan) 브랜드 요소 — 로그인/회원가입 등 인증 화면에서 공유. */

/** 파란 라운드 사각형 안에 "시" 로고. */
export function BrandDot({ size = 22 }: { size?: number }) {
  return (
    <span
      className="grid place-items-center rounded-[7px] bg-primary font-extrabold text-white shadow-[0_4px_10px_rgba(61,106,255,0.35)]"
      style={{ width: size, height: size, fontSize: size * 0.55 }}
    >
      シ
    </span>
  )
}

/** 로고 + "시간표" 워드마크. */
export function BrandMark({ size = 22 }: { size?: number }) {
  return (
    <div className="flex items-center gap-2 font-extrabold tracking-tight" style={{ fontSize: size * 0.8 }}>
      <BrandDot size={size} />
      シフト表
    </div>
  )
}

/** primary 톤 pill 칩. */
export function Chip({ children }: { children: React.ReactNode }) {
  return (
    <span className="inline-flex h-[26px] items-center gap-1.5 rounded-full bg-[#EEF3FF] px-2.5 text-xs font-semibold text-primary">
      {children}
    </span>
  )
}
