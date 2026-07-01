import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { LockKeyhole } from 'lucide-react';
import { useRole } from '../context/RoleContext';
import { canAccess } from '../data/roles';
import Card from './ui/Card';

export function ProtectedRoute({ routeKey, children }) {
  const { activeRole } = useRole();
  const location = useLocation();

  if (!canAccess(activeRole.key, routeKey)) {
    return (
      <div className="space-y-6">
        <Card className="p-8 text-center">
          <div className="mx-auto grid h-14 w-14 place-items-center rounded-2xl bg-brand-blueAccent text-brand-secondary">
            <LockKeyhole className="h-6 w-6" />
          </div>
          <h1 className="mt-5 text-2xl font-extrabold text-ink-primary">Access limited for {activeRole.label}</h1>
          <p className="mx-auto mt-2 max-w-xl text-sm leading-6 text-ink-secondary">
            This page is hidden for the selected mock role. Switch roles from the top bar to preview another permission level.
          </p>
        </Card>
      </div>
    );
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
