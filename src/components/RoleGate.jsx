import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useRole } from '../context/RoleContext';
import { canAccess } from '../data/roles';

export function ProtectedRoute({ routeKey, children }) {
  const { activeRole } = useRole();
  const location = useLocation();

  if (!canAccess(activeRole.key, routeKey)) {
    return <Navigate to="/dashboard" replace state={{ from: location.pathname }} />;
  }

  if (location.pathname === '/') {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

export function Can({ roles: allowedRoles, children, fallback = null }) {
  const { activeRole } = useRole();
  return allowedRoles.includes(activeRole.key) ? children : fallback;
}
