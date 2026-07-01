import React from "react";
import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './components/RoleGate';
import AppLayout from './components/layout/AppLayout';
import { RoleProvider } from './context/RoleContext';
import { AuthProvider } from './context/AuthContext';
import AdminConsole from './pages/AdminConsole';
import Attendance from './pages/Attendance';
import DailyReports from './pages/DailyReports';
import Dashboard from './pages/Dashboard';
import Login from './pages/Login';
import Notifications from './pages/Notifications';
import Profile from './pages/Profile';
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
            <Route path="/admin" element={<ProtectedRoute routeKey="admin"><AdminConsole /></ProtectedRoute>} />
            <Route path="/attendance" element={<ProtectedRoute routeKey="attendance"><Attendance /></ProtectedRoute>} />
            <Route path="/tasks" element={<ProtectedRoute routeKey="tasks"><Tasks /></ProtectedRoute>} />
            <Route path="/reports" element={<ProtectedRoute routeKey="reports"><DailyReports /></ProtectedRoute>} />
            <Route path="/team" element={<ProtectedRoute routeKey="team"><TeamDirectory /></ProtectedRoute>} />
            <Route path="/notifications" element={<ProtectedRoute routeKey="notifications"><Notifications /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute routeKey="profile"><Profile /></ProtectedRoute>} />
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </RoleProvider>
    </AuthProvider>
  );
}
