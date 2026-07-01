import React, { createContext, useContext, useMemo, useState } from 'react';
import { useAuth } from './AuthContext';
import { frontendRoleFromBackend, roles } from '../data/roles';

const RoleContext = createContext(null);

export function RoleProvider({ children }) {
  const { authUser, isAuthenticated } = useAuth();
  const [activeRoleKey, setActiveRoleKey] = useState(() => localStorage.getItem('nexstar-role') || 'admin');
  const backendRoleKey = frontendRoleFromBackend(authUser?.roles || []);
  const effectiveRoleKey = isAuthenticated ? backendRoleKey : activeRoleKey;
  const activeRole = roles[effectiveRoleKey] || roles.employee;

  const value = useMemo(
    () => ({
      activeRole,
      activeRoleKey: effectiveRoleKey,
      previewRoleKey: activeRoleKey,
      isBackendRole: isAuthenticated,
      setActiveRoleKey: (roleKey) => {
        localStorage.setItem('nexstar-role', roleKey);
        setActiveRoleKey(roleKey);
      },
    }),
    [activeRole, activeRoleKey, effectiveRoleKey, isAuthenticated],
  );

  return <RoleContext.Provider value={value}>{children}</RoleContext.Provider>;
}

export function useRole() {
  const context = useContext(RoleContext);
  if (!context) {
    throw new Error('useRole must be used inside RoleProvider');
  }
  return context;
}
