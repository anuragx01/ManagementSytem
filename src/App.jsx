import React from "react";
import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './components/RoleGate';
import AppLayout from './components/layout/AppLayout';
import { RoleProvider } from './context/RoleContext';
import { AuthProvider } from './context/AuthContext';
import Attendance from './pages/Attendance';
import AdminConsole from './pages/AdminConsole';
import AuditLogs from './pages/AuditLogs';
import Calendar from './pages/Calendar';
import Dashboard from './pages/Dashboard';
import Departments from './pages/Departments';
import Holidays from './pages/Holidays';
import Leave from './pages/Leave';
import Login from './pages/Login';
import Notifications from './pages/Notifications';
import Organization from './pages/Organization';
import Payroll from './pages/Payroll';
import Profile from './pages/Profile';
import Projects from './pages/Projects';
import Reports from './pages/Reports';
import DailyReports from './pages/DailyReports';
import Settings from './pages/Settings';
import Tasks from './pages/Tasks';
import TeamDirectory from './pages/TeamDirectory';

export default function App() {
  return (
    <AuthProvider>
      <RoleProvider>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route element={<AppLayout />}>
            <Route path="/dashboard" element={<ProtectedRoute routeKey="dashboard"><Dashboard /></ProtectedRoute>} />
            <Route path="/attendance" element={<ProtectedRoute routeKey="attendance"><Attendance /></ProtectedRoute>} />
            <Route path="/leave" element={<ProtectedRoute routeKey="leave"><Leave /></ProtectedRoute>} />
            <Route path="/tasks" element={<ProtectedRoute routeKey="tasks"><Tasks /></ProtectedRoute>} />
            <Route path="/calendar" element={<ProtectedRoute routeKey="calendar"><Calendar /></ProtectedRoute>} />
            <Route path="/holidays" element={<ProtectedRoute routeKey="holidays"><Holidays /></ProtectedRoute>} />
            <Route path="/notifications" element={<ProtectedRoute routeKey="notifications"><Notifications /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute routeKey="profile"><Profile /></ProtectedRoute>} />
            <Route path="/employees" element={<ProtectedRoute routeKey="employees"><TeamDirectory /></ProtectedRoute>} />
            <Route path="/team" element={<ProtectedRoute routeKey="team"><TeamDirectory /></ProtectedRoute>} />
            <Route path="/departments" element={<ProtectedRoute routeKey="departments"><Departments /></ProtectedRoute>} />
            <Route path="/departments/:departmentId" element={<ProtectedRoute routeKey="departments"><Departments /></ProtectedRoute>} />
            <Route path="/organization" element={<ProtectedRoute routeKey="organization"><Organization /></ProtectedRoute>} />
            <Route path="/projects" element={<ProtectedRoute routeKey="projects"><Projects /></ProtectedRoute>} />
            <Route path="/projects/:projectId" element={<ProtectedRoute routeKey="projects"><Projects /></ProtectedRoute>} />
            <Route path="/reports" element={<ProtectedRoute routeKey="reports"><Reports /></ProtectedRoute>} />
            <Route path="/payroll" element={<ProtectedRoute routeKey="payroll"><Payroll /></ProtectedRoute>} />
            <Route path="/admin-console" element={<ProtectedRoute routeKey="adminConsole"><AdminConsole /></ProtectedRoute>} />
            <Route path="/audit-logs" element={<ProtectedRoute routeKey="auditLogs"><AuditLogs /></ProtectedRoute>} />
            <Route path="/daily-reports" element={<ProtectedRoute routeKey="dailyReport"><DailyReports /></ProtectedRoute>} />
            <Route path="/settings" element={<ProtectedRoute routeKey="settings"><Settings /></ProtectedRoute>} />
            <Route path="/admin" element={<Navigate to="/dashboard" replace />} />
            <Route path="/reports/daily" element={<Navigate to="/daily-reports" replace />} />
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </RoleProvider>
    </AuthProvider>
  );
}
