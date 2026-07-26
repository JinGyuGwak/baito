/**
 * 시간표(Shijan) 아이콘 세트 — 디자인 시스템(icons.jsx)에서 포팅.
 * 최소 stroke SVG. 색은 currentColor, 크기는 size prop.
 */
import type { SVGProps } from 'react'

interface IconProps extends Omit<SVGProps<SVGSVGElement>, 'stroke'> {
  size?: number
  stroke?: number
}

function Icon({ size = 18, stroke = 1.8, children, ...props }: IconProps & { children: React.ReactNode }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={stroke}
      strokeLinecap="round"
      strokeLinejoin="round"
      style={{ flexShrink: 0 }}
      {...props}
    >
      {children}
    </svg>
  )
}

export const IconCheck = (p: IconProps) => (
  <Icon {...p}>
    <path d="M4 12.5l5 5L20 6" />
  </Icon>
)
export const IconArrow = (p: IconProps) => (
  <Icon {...p}>
    <path d="M5 12h14M13 6l6 6-6 6" />
  </Icon>
)
export const IconStore = (p: IconProps) => (
  <Icon {...p}>
    <path d="M3 9l1.5-5h15L21 9M3 9v11h18V9M3 9c0 1.7 1.3 3 3 3s3-1.3 3-3 1.3 3 3 3 3-1.3 3-3 1.3 3 3 3 3-1.3 3-3M10 20v-6h4v6" />
  </Icon>
)
export const IconUsers = (p: IconProps) => (
  <Icon {...p}>
    <circle cx="9" cy="8" r="3.5" />
    <path d="M2 21c0-3.9 3.1-7 7-7s7 3.1 7 7" />
    <circle cx="17" cy="9" r="2.8" />
    <path d="M22 19c0-2.7-2-5-4.7-5" />
  </Icon>
)
export const IconClock = (p: IconProps) => (
  <Icon {...p}>
    <circle cx="12" cy="12" r="9" />
    <path d="M12 7v5l3 2" />
  </Icon>
)
export const IconWarn = (p: IconProps) => (
  <Icon {...p}>
    <path d="M12 3l10 18H2L12 3z" />
    <path d="M12 10v5M12 18.5v.1" />
  </Icon>
)
export const IconPlus = (p: IconProps) => (
  <Icon {...p}>
    <path d="M12 5v14M5 12h14" />
  </Icon>
)
export const IconX = (p: IconProps) => (
  <Icon {...p}>
    <path d="M6 6l12 12M18 6L6 18" />
  </Icon>
)
export const IconCalendar = (p: IconProps) => (
  <Icon {...p}>
    <rect x="3" y="5" width="18" height="16" rx="2" />
    <path d="M16 3v4M8 3v4M3 10h18" />
  </Icon>
)
export const IconMail = (p: IconProps) => (
  <Icon {...p}>
    <rect x="3" y="5" width="18" height="14" rx="2" />
    <path d="M3 7l9 7 9-7" />
  </Icon>
)
export const IconBell = (p: IconProps) => (
  <Icon {...p}>
    <path d="M6 19V11a6 6 0 0112 0v8M3 19h18M10 22h4" />
  </Icon>
)
export const IconSearch = (p: IconProps) => (
  <Icon {...p}>
    <circle cx="11" cy="11" r="7" />
    <path d="M20 20l-3.5-3.5" />
  </Icon>
)
export const IconChevL = (p: IconProps) => (
  <Icon {...p}>
    <path d="M15 6l-6 6 6 6" />
  </Icon>
)
export const IconChevR = (p: IconProps) => (
  <Icon {...p}>
    <path d="M9 6l6 6-6 6" />
  </Icon>
)
export const IconMore = (p: IconProps) => (
  <Icon {...p}>
    <circle cx="5" cy="12" r="1.4" />
    <circle cx="12" cy="12" r="1.4" />
    <circle cx="19" cy="12" r="1.4" />
  </Icon>
)
export const IconSettings = (p: IconProps) => (
  <Icon {...p}>
    <circle cx="12" cy="12" r="3" />
    <path d="M19.4 15a1.7 1.7 0 00.3 1.8l.1.1a2 2 0 11-2.8 2.8l-.1-.1a1.7 1.7 0 00-1.8-.3 1.7 1.7 0 00-1 1.5V21a2 2 0 01-4 0v-.1a1.7 1.7 0 00-1.1-1.5 1.7 1.7 0 00-1.8.3l-.1.1a2 2 0 11-2.8-2.8l.1-.1a1.7 1.7 0 00.3-1.8 1.7 1.7 0 00-1.5-1H3a2 2 0 010-4h.1a1.7 1.7 0 001.5-1.1 1.7 1.7 0 00-.3-1.8l-.1-.1a2 2 0 112.8-2.8l.1.1a1.7 1.7 0 001.8.3H9a1.7 1.7 0 001-1.5V3a2 2 0 014 0v.1a1.7 1.7 0 001 1.5 1.7 1.7 0 001.8-.3l.1-.1a2 2 0 112.8 2.8l-.1.1a1.7 1.7 0 00-.3 1.8V9a1.7 1.7 0 001.5 1H21a2 2 0 010 4h-.1a1.7 1.7 0 00-1.5 1z" />
  </Icon>
)
export const IconSparkle = (p: IconProps) => (
  <Icon {...p}>
    <path d="M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8L12 3z" />
    <path d="M19 17l.7 2 2 .7-2 .7L19 22.5l-.7-2-2-.7 2-.7z" />
  </Icon>
)
export const IconLogout = (p: IconProps) => (
  <Icon {...p}>
    <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9" />
  </Icon>
)
export const IconTrash = (p: IconProps) => (
  <Icon {...p}>
    <path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v6M14 11v6" />
  </Icon>
)
export const IconCopy = (p: IconProps) => (
  <Icon {...p}>
    <rect x="8" y="8" width="13" height="13" rx="2" />
    <path d="M16 8V4a2 2 0 00-2-2H5a2 2 0 00-2 2v9a2 2 0 002 2h3" />
  </Icon>
)
export const IconDrag = (p: IconProps) => (
  <Icon {...p}>
    <circle cx="9" cy="5" r="1.2" />
    <circle cx="15" cy="5" r="1.2" />
    <circle cx="9" cy="12" r="1.2" />
    <circle cx="15" cy="12" r="1.2" />
    <circle cx="9" cy="19" r="1.2" />
    <circle cx="15" cy="19" r="1.2" />
  </Icon>
)
