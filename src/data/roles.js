import { getNavigationForRole } from '../config/navigation';

export const roles = {
  admin: {
    key: 'admin',
    label: 'Admin',
    backendRoles: ['SUPER_ADMIN'],
    title: 'Organization administration',
    routes: [
      'dashboard', 'employees', 'departments', 'attendance', 'leave',
      'projects', 'tasks', 'reports', 'payroll', 'adminConsole', 'settings', 'profile',
    ],
  },
  hr: {
    key: 'hr',
    label: 'HR',
    backendRoles: ['HR'],
    title: 'People operations',
    routes: [
      'dashboard', 'employees', 'attendance', 'leave', 'departments',
      'calendar', 'reports', 'payroll', 'organization', 'notifications', 'settings', 'profile',
    ],
  },
  manager: {
    key: 'manager',
    label: 'Manager',
    backendRoles: ['MANAGER'],
    title: 'Team management',
    routes: [
      'dashboard', 'employees', 'attendance', 'leave', 'projects',
      'tasks', 'reports', 'calendar', 'notifications', 'profile',
    ],
  },
  team_lead: {
    key: 'team_lead',
    label: 'Team Lead',
    backendRoles: ['TEAM_LEAD'],
    title: 'Team delivery',
    routes: [
      'dashboard', 'tasks', 'projects', 'attendance', 'calendar', 'notifications', 'profile',
    ],
  },
  employee: {
    key: 'employee',
    label: 'Employee',
    backendRoles: ['EMPLOYEE'],
    title: 'Self-service workspace',
    routes: [
      'dashboard', 'attendance', 'leave', 'tasks', 'dailyReport', 'team', 'calendar',
      'holidays', 'notifications', 'payroll', 'settings', 'profile',
    ],
  },
};

export const roleOptions = Object.values(roles);

export function canAccess(roleKey, routeKey) {
  return roles[roleKey]?.routes.includes(routeKey) ?? false;
}

export function frontendRoleFromBackend(backendRoles = []) {
  if (backendRoles.includes('SUPER_ADMIN')) return 'admin';
  if (backendRoles.includes('HR')) return 'hr';
  if (backendRoles.includes('MANAGER')) return 'manager';
  if (backendRoles.includes('TEAM_LEAD')) return 'team_lead';
  return 'employee';
}

export function getRoleNavigation(roleKey) {
  return getNavigationForRole(roleKey);
}

export function hasBackendRole(activeRole, backendRole) {
  return activeRole.backendRoles.includes(backendRole);
}
