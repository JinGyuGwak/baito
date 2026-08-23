import { Link, useLocation, useNavigate } from 'react-router-dom'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { BrandMark } from '@/components/brand'
import { IconBell, IconLogout, IconSearch, IconSettings } from '@/components/icons'

export interface NavItem {
  label: string
  to: string
}

interface AppBarProps {
  navItems: NavItem[]
  name: string
  roleLabel: string
  onLogout: () => void
}

export function AppBar({ navItems, name, roleLabel, onLogout }: AppBarProps) {
  const { pathname } = useLocation()
  const navigate = useNavigate()

  return (
    <header className="flex h-[60px] items-center justify-between border-b border-border bg-white px-7">
      <div className="flex items-center gap-7">
        <BrandMark size={22} />
        <nav className="flex gap-1">
          {navItems.map((item) => {
            const active = item.to === '/' ? pathname === '/' : pathname.startsWith(item.to)
            return (
              <Link
                key={item.label}
                to={item.to}
                className={[
                  'rounded-lg px-3.5 py-2 text-sm font-semibold',
                  active ? 'bg-secondary text-foreground' : 'text-muted-foreground hover:bg-secondary/60',
                ].join(' ')}
              >
                {item.label}
              </Link>
            )
          })}
        </nav>
      </div>

      <div className="flex items-center gap-3.5">
        <IconButton aria-label="検索">
          <IconSearch size={18} />
        </IconButton>
        <IconButton aria-label="通知">
          <IconBell size={18} />
          <span className="absolute right-2 top-2 h-[7px] w-[7px] rounded-full border-[1.5px] border-white bg-[#F04452]" />
        </IconButton>

        <DropdownMenu>
          <DropdownMenuTrigger className="ml-1 flex items-center gap-2 border-l border-border pl-3 outline-none">
            <Avatar name={name} />
            <div className="flex flex-col items-start leading-tight">
              <span className="text-[13px] font-bold">{name}</span>
              <span className="text-[11px] text-muted-foreground">{roleLabel}</span>
            </div>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-44">
            <DropdownMenuItem onClick={() => navigate('/profile')}>
              <IconSettings size={16} />
              プロフィール
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={onLogout} className="text-[#F04452] focus:text-[#F04452]">
              <IconLogout size={16} />
              ログアウト
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}

function IconButton({ children, ...props }: React.HTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      type="button"
      className="relative grid h-9 w-9 place-items-center rounded-[10px] text-muted-foreground hover:bg-secondary"
      {...props}
    >
      {children}
    </button>
  )
}

export function Avatar({ name, size = 32 }: { name: string; size?: number }) {
  return (
    <span
      className="grid place-items-center rounded-full bg-[linear-gradient(135deg,#6E8BFF,#3D6AFF)] font-bold text-white"
      style={{ width: size, height: size, fontSize: size * 0.375 }}
    >
      {name.slice(0, 1).toUpperCase()}
    </span>
  )
}
