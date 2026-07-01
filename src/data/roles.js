export const roles = {
  admin: {
    key: 'admin',
    label: 'Admin',
    backendRoles: ['SUPER_ADMIN'],
    title: 'Complete system access',
    permissions: [
      'Add, edit, and delete employees',
      'Create departments',
      'Assign tasks',
      'Monitor employee activities',
      'View reports',
      'Manage announcements',
    ],
    routes: ['dashboard', 'admin', 'attendance', 'tasks', 'reports', 'team', 'notifications', 'profile'],
  },
  hr: {
    key: 'hr',
    label: 'HR',
    backendRoles: ['HR'],
    title: 'People operations access',
    permissions: [
      'Add employees',
      'Update employee details',
      'View attendance',
      'Manage employee information',
    ],
    routes: ['dashboard', 'attendance', 'team', 'profile'],
  },
  manager: {
    key: 'manager',
    label: 'Team Lead / Manager',
    backendRoles: ['MANAGER', 'TEAM_LEAD'],
    title: 'Team performance access',
    permissions: [
      'Assign tasks',
      'Review completed tasks',
      'Monitor team performance',
      'View employee progress',
    ],
    routes: ['dashboard', 'tasks', 'reports', 'team', 'profile'],
  },
  employee: {
    key: 'employee',
    label: 'Employee',
    backendRoles: ['EMPLOYEE'],
    title: 'Self-service access',
    permissions: [
      'Login and logout',
      'View and edit profile',
      'View assigned tasks',
      'Update task status',
      'Submit daily work report',
      'View notifications',
    ],
    routes: ['dashboard', 'tasks', 'reports', 'notifications', 'profile'],
  },
};

export const roleOptions = Object.values(roles);

export function canAccess(roleKey, routeKey) {
  return roles[roleKey]?.routes.includes(routeKey);
}

export function frontendRoleFromBackend(backendRoles = []) {
  if (backendRoles.includes('SUPER_ADMIN')) return 'admin';
  if (backendRoles.includes('HR')) return 'hr';
  if (backendRoles.includes('MANAGER') || backendRoles.includes('TEAM_LEAD')) return 'manager';
  return 'employee';
}
