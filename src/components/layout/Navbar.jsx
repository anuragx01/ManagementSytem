import React from "react";
import { Link } from 'react-router-dom';
import { Bell, Search } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useRole } from '../../context/RoleContext';
import { roleOptions } from '../../data/roles';
import useApiData from '../../hooks/useApiData';
import { notificationsApi } from '../../lib/api';
import LogoMark from '../ui/LogoMark';

const defaultAvatar = 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=160&q=80';

export default function Navbar() {
  const { authUser, isAuthenticated } = useAuth();
  const { activeRole, activeRoleKey, previewRoleKey, isBackendRole, setActiveRoleKey } = useRole();
  const { data: unreadCount } = useApiData(
    () => notificationsApi.unreadCount(),
    0,
    [],
  );

  const displayUser = authUser
    ? {
        name: `${authUser.firstName || ''} ${authUser.lastName || ''}`.trim() || authUser.email,
        avatar: authUser.profilePictureUrl || defaultAvatar,
      }
    : { name: 'Guest', avatar: defaultAvatar };

  const badgeCount = typeof unreadCount === 'number' ? unreadCount : unreadCount?.count ?? 0;

  return (
    <header className="sticky top-0 z-30 border-b border-line bg-white/[0.92] shadow-[0_1px_0_rgba(16,24,40,0.02)] backdrop-blur-xl">
      <div className="grid min-h-20 grid-cols-[auto_1fr_auto] items-center gap-4 px-4 py-3 sm:px-6 lg:px-8">
        <div className="flex min-w-0 items-center gap-3 lg:pl-0 pl-12">
          <LogoMark size="sm" />
          <div className="hidden sm:block">
            <p className="text-base font-extrabold leading-tight text-ink-primary">NEXSTAR</p>
            <p className="text-xs font-semibold text-ink-secondary">Employee Suite</p>
          </div>
        </div>

        <div className="flex min-w-0 items-center justify-center">
          <label className="relative hidden w-full max-w-4xl md:block">
            <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-secondary" aria-hidden="true" />
            <input
              className="field-control bg-surface-muted py-3 pl-11"
              placeholder="Search employees, tasks, reports..."
              aria-label="Search employees, tasks, reports"
            />
          </label>
        </div>

        <div className="flex min-w-0 items-center justify-end gap-2">
          <select
            className="select-control hidden max-w-52 md:block"
            value={isBackendRole ? activeRoleKey : previewRoleKey}
            onChange={(event) => setActiveRoleKey(event.target.value)}
            disabled={isBackendRole}
            aria-label="Select role"
          >
            {roleOptions.map((role) => (
              <option key={role.key} value={role.key}>{role.label}</option>
            ))}
          </select>
          <Link to="/notifications" className="icon-button relative" aria-label="Notifications">
            <Bell className="h-5 w-5 text-ink-secondary" />
            {badgeCount > 0 && (
              <span className="absolute -right-0.5 -top-0.5 grid min-h-5 min-w-5 place-items-center rounded-full bg-brand-accent px-1 text-[10px] font-bold text-white ring-2 ring-white">
                {badgeCount > 99 ? '99+' : badgeCount}
              </span>
            )}
          </Link>
          <div className="flex min-w-0 items-center gap-3 rounded-2xl border border-line bg-white p-2 shadow-sm">
            <img src={displayUser.avatar} alt={displayUser.name} className="h-10 w-10 rounded-xl object-cover" />
            <div className="hidden pr-2 sm:block">
              <p className="max-w-36 truncate text-sm font-bold text-ink-primary">{displayUser.name}</p>
              <p className="text-xs text-ink-secondary">{isAuthenticated ? `${activeRole.label} role` : `${activeRole.label} preview`}</p>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
