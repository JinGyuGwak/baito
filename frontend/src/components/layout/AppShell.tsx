import { Outlet } from 'react-router-dom'
import { AppBar, type NavItem } from './AppBar'
import { Sidebar, type SidebarGroup } from './Sidebar'
import { useAuthStore, useLogoutMutation } from '@/features/auth'
import { useGroupsQuery } from '@/features/groups'
import { useMyGroupsQuery } from '@/features/memberships'
import { useMyProfileQuery } from '@/features/member'

/**
 * 인증 영역 공통 레이아웃 — 상단 AppBar + 좌측 Sidebar + 본문(Outlet).
 * 역할에 따라 사이드바 그룹 소스가 다름(점주=소유 그룹, 알바=소속 그룹).
 */
export function AppShell() {
  const user = useAuthStore((s) => s.user)
  const logout = useLogoutMutation()
  const isOwner = user?.role === 'OWNER'

  // 표시용 이름(로그인 응답엔 없음). 프로필을 한 번 조회해 이름을 확보한다.
  const profile = useMyProfileQuery()
  const displayName = profile.data?.name ?? user?.name ?? user?.loginId ?? '사용자'

  // 역할에 해당하는 그룹 엔드포인트만 호출(반대 역할 엔드포인트는 403이므로 비활성).
  const ownerGroups = useGroupsQuery({ enabled: isOwner })
  const memberGroups = useMyGroupsQuery({ enabled: !isOwner })
  const groups: SidebarGroup[] = (isOwner ? ownerGroups.data : memberGroups.data) ?? []

  const groupHref = (groupId: number) =>
    isOwner ? `/groups/${groupId}/schedule` : `/groups/${groupId}/availability`

  const navItems: NavItem[] = [{ label: '대시보드', to: '/' }]
  if (groups.length > 0) {
    navItems.push({ label: isOwner ? '스케줄' : '근무 가능 시간', to: groupHref(groups[0].id) })
  }
  if (!isOwner) navItems.push({ label: '내 스케줄', to: '/my-schedule' })

  return (
    <div className="min-h-screen bg-[hsl(var(--bg-soft))] text-foreground">
      <AppBar
        navItems={navItems}
        name={displayName}
        roleLabel={isOwner ? '점주' : '아르바이트생'}
        onLogout={() => logout.mutate()}
      />
      <div className="grid grid-cols-[220px_1fr]">
        <Sidebar
          title={isOwner ? '내 그룹' : '소속 그룹'}
          groups={groups}
          groupHref={groupHref}
          createHref={isOwner ? '/?create=1' : undefined}
        />
        <main className="min-h-[calc(100vh-60px)]">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
