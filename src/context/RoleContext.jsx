import React, { createContext, useContext, useMemo } from 'react';
import { useSelector } from 'react-redux';
import { selectAuthUser, selectIsAuthenticated } from '../features/auth/authSlice';
import { frontendRoleFromBackend, roles } from '../data/roles';

const RoleContext = createContext(null);

export function RoleProvider({ children }) {
  const authUser = useSelector(selectAuthUser);
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const effectiveRoleKey = frontendRoleFromBackend(authUser?.roles || []);
  const activeRole = roles[effectiveRoleKey] || roles.employee;

  const value = useMemo(
    () => ({
      activeRole,
      activeRoleKey: effectiveRoleKey,
      previewRoleKey: null,
      isBackendRole: isAuthenticated,
      setActiveRoleKey: () => {},
    }),
    [activeRole, effectiveRoleKey, isAuthenticated],
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

