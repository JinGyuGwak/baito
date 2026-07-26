import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { useAuthStore } from '@/features/auth'
import { AppShell } from '@/components/layout/AppShell'
import { LoginPage } from '@/pages/LoginPage'
import { SignupPage } from '@/pages/SignupPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { SchedulePage } from '@/pages/SchedulePage'
import { AssignmentPage } from '@/pages/AssignmentPage'
import { AvailabilityPage } from '@/pages/AvailabilityPage'
import { InvitationsPage } from '@/pages/InvitationsPage'
import { MySchedulePage } from '@/pages/MySchedulePage'

/** 토큰이 없으면 로그인으로 보내는 보호 라우트. */
function ProtectedRoute() {
  const token = useAuthStore((s) => s.token)
  if (!token) return <Navigate to="/login" replace />
  return <Outlet />
}

export function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      {/* 인증 영역 — AppShell(상단바+사이드바) 안에서 렌더 */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/my-schedule" element={<MySchedulePage />} />
          <Route path="/groups/:groupId/schedule" element={<SchedulePage />} />
          <Route path="/groups/:groupId/assignments" element={<AssignmentPage />} />
          <Route path="/groups/:groupId/availability" element={<AvailabilityPage />} />
          <Route path="/groups/:groupId/invitations" element={<InvitationsPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
