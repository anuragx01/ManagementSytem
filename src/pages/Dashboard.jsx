import React from 'react';
import { useRole } from '../context/RoleContext';
import AdminDashboard from './dashboards/AdminDashboard';
import EmployeeDashboard from './dashboards/EmployeeDashboard';
import HrDashboard from './dashboards/HrDashboard';
import ManagerDashboard from './dashboards/ManagerDashboard';
import TeamLeadDashboard from './dashboards/TeamLeadDashboard';

const dashboards = {
  admin: AdminDashboard,
  hr: HrDashboard,
  manager: ManagerDashboard,
  team_lead: TeamLeadDashboard,
  employee: EmployeeDashboard,
};

export default function Dashboard() {
  const { activeRole } = useRole();
  const View = dashboards[activeRole.key] || EmployeeDashboard;
  return <View />;
}

