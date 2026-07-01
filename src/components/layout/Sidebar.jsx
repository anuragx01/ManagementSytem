import React from "react";
import { NavLink } from 'react-router-dom';
import {
  Bell,
  CalendarCheck,
  ClipboardList,
  LayoutDashboard,
  LogOut,
  NotebookTabs,
  ShieldCheck,
  User,
  Users,
  X,
} from 'lucide-react';
import LogoMark from '../ui/LogoMark';
import { useAuth } from '../../context/AuthContext';
import { useRole } from '../../context/RoleContext';
import { canAccess } from '../../data/roles';

const items = [
  { label: 'Dashboard', path: '/dashboard', routeKey: 'dashboard', icon: LayoutDashboard },
  { label: 'Admin Console', path: '/admin', routeKey: 'admin', icon: ShieldCheck },
  { label: 'Attendance', path: '/attendance', routeKey: 'attendance', icon: CalendarCheck },
  { label: 'Tasks', path: '/tasks', routeKey: 'tasks', icon: ClipboardList },
  { label: 'Daily Reports', path: '/reports', routeKey: 'reports', icon: NotebookTabs },
  { label: 'Team Directory', path: '/team', routeKey: 'team', icon: Users },
  { label: 'Notifications', path: '/notifications', routeKey: 'notifications', icon: Bell },
  { label: 'Profile', path: '/profile', routeKey: 'profile', icon: User },
];

export default function Sidebar({ isOpen, onClose }) {
  const { logout } = useAuth();
  const { activeRole } = useRole();
  const visibleItems = items.filter((item) => canAccess(activeRole.key, item.routeKey));
  const linkClass = ({ isActive }) =>
    `flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-semibold transition ${
      isActive ? 'bg-brand-accent text-white shadow-glow' : 'text-slate-200 hover:bg-white/10 hover:text-white'
    }`;

  return (
    <>
      <div
        className={`fixed inset-0 z-40 bg-ink-primary/30 transition ${isOpen ? 'opacity-100' : 'pointer-events-none opacity-0'}`}
        onClick={onClose}
      />
      <aside
        className={`fixed left-0 top-0 z-50 h-full w-72 border-r border-brand-secondary bg-brand-primary px-5 py-6 transition-transform duration-300 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        } lg:w-72 xl:w-76`}
      >
        <div className="mb-8 flex items-center justify-between">
          <NavLink to="/dashboard" className="flex items-center gap-3" onClick={onClose}>
            <LogoMark size="sm" />
            <div>
              <p className="text-lg font-extrabold text-white">NEXSTAR</p>
              <p className="text-xs font-medium text-slate-300">Employee Suite</p>
            </div>
          </NavLink>
          <button className="rounded-xl p-2 text-white hover:bg-white/10" onClick={onClose} aria-label="Close navigation">
            <X className="h-5 w-5" />
          </button>
        </div>

        <nav className="space-y-2">
          {visibleItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink key={item.path} to={item.path} className={linkClass} onClick={onClose}>
                <Icon className="h-5 w-5" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        <div className="absolute bottom-6 left-5 right-5">
          <div className="mb-3 rounded-2xl bg-white/10 p-4">
            <p className="text-xs font-bold uppercase text-brand-redSoft">Current Role</p>
            <p className="mt-1 text-sm font-extrabold text-white">{activeRole.label}</p>
          </div>
          <NavLink
            to="/"
            onClick={() => {
              logout();
              onClose();
            }}
            className="flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-semibold text-slate-200 transition hover:bg-white/10 hover:text-white"
          >
            <LogOut className="h-5 w-5" />
            <span>Logout</span>
          </NavLink>
        </div>
      </aside>
    </>
  );
}
