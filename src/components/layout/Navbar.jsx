import React from "react";
import { Bell, Search, Settings } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useRole } from '../../context/RoleContext';
import { currentUser } from '../../data/mockData';
import { roleOptions } from '../../data/roles';
import LogoMark from '../ui/LogoMark';

export default function Navbar() {
  const { authUser, isAuthenticated } = useAuth();
  const { activeRole, activeRoleKey, previewRoleKey, isBackendRole, setActiveRoleKey } = useRole();
  const displayUser = authUser
    ? {
        name: `${authUser.firstName || ''} ${authUser.lastName || ''}`.trim() || authUser.email,
        avatar: authUser.profilePictureUrl || currentUser.avatar,
      }
    : currentUser;

  return (
    <header className="sticky top-0 z-30 border-b border-line bg-white/95 backdrop-blur">
      <div className="grid h-20 grid-cols-[auto_1fr_auto] items-center gap-4 px-5 lg:px-8">
        <div className="flex min-w-0 items-center gap-3">
          <LogoMark size="sm" />
          <div className="hidden sm:block">
            <p className="text-base font-extrabold leading-tight text-ink-primary">NEXSTAR</p>
            <p className="text-xs font-semibold text-ink-secondary">Employee Suite</p>
          </div>
        </div>

        <div className="flex min-w-0 items-center justify-center">
          <label className="relative hidden w-full max-w-4xl md:block">
            <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-secondary" />
            <input
              className="w-full rounded-2xl border border-line bg-surface-muted py-3 pl-11 pr-4 text-sm outline-none transition focus:border-brand-accent focus:bg-white focus:ring-4 focus:ring-brand-redSoft"
              placeholder="Search employees, tasks, reports..."
            />
          </label>
        </div>

        <div className="flex items-center justify-end gap-2">
          <select
            className="hidden rounded-2xl border border-line bg-white px-4 py-3 text-sm font-bold text-ink-primary outline-none transition focus:border-brand-accent focus:ring-4 focus:ring-brand-redSoft md:block"
            value={isBackendRole ? activeRoleKey : previewRoleKey}
            onChange={(event) => setActiveRoleKey(event.target.value)}
            disabled={isBackendRole}
            aria-label="Select role"
          >
            {roleOptions.map((role) => (
              <option key={role.key} value={role.key}>{role.label}</option>
            ))}
          </select>
          <button className="relative rounded-2xl border border-line bg-white p-3 transition hover:bg-brand-blueAccent" aria-label="Notifications">
            <Bell className="h-5 w-5 text-ink-secondary" />
            <span className="absolute right-2 top-2 h-2.5 w-2.5 rounded-full bg-brand-accent ring-2 ring-white" />
          </button>
          <button className="rounded-2xl border border-line bg-white p-3 transition hover:bg-brand-blueAccent" aria-label="Settings">
            <Settings className="h-5 w-5 text-ink-secondary" />
          </button>
          <div className="flex items-center gap-3 rounded-2xl border border-line bg-white p-2">
            <img src={displayUser.avatar} alt={displayUser.name} className="h-10 w-10 rounded-xl object-cover" />
            <div className="hidden pr-2 sm:block">
              <p className="text-sm font-bold text-ink-primary">{displayUser.name}</p>
              <p className="text-xs text-ink-secondary">{isAuthenticated ? `${activeRole.label} role` : `${activeRole.label} preview`}</p>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
