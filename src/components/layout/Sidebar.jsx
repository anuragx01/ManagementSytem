import React from 'react';
import { NavLink } from 'react-router-dom';
import { LogOut, Menu, X } from 'lucide-react';
import LogoMark from '../ui/LogoMark';
import { useAuth } from '../../context/AuthContext';
import { useRole } from '../../context/RoleContext';
import { getRoleNavigation } from '../../data/roles';

function SidebarPanel({ items, activeRole, onNavigate, onLogout, className = '' }) {
  const linkClass = ({ isActive }) =>
    `flex min-h-11 items-center gap-3 rounded-2xl px-4 py-3 text-sm font-bold transition duration-200 ease-out focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-white/20 ${
      isActive ? 'bg-brand-accent text-white shadow-glow' : 'text-slate-200 hover:bg-white/10 hover:text-white'
    }`;

  return (
    <aside className={`${className} overflow-hidden`} aria-label="Primary navigation">
      <div className="flex h-full flex-col px-4 py-6 xl:px-5">
        <div className="mb-8 flex items-center justify-between">
          <NavLink to="/dashboard" className="flex items-center gap-3 rounded-xl focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-white/20" onClick={onNavigate}>
            <LogoMark size="sm" />
            <div>
              <p className="text-lg font-extrabold text-white">NEXSTAR</p>
              <p className="text-xs font-medium text-slate-300">Employee Suite</p>
            </div>
          </NavLink>
        </div>

        <nav className="no-scrollbar flex-1 space-y-2 overflow-y-auto overflow-x-hidden overscroll-contain">
          {items.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink key={item.path} to={item.path} className={linkClass} onClick={onNavigate}>
                <Icon className="h-5 w-5 shrink-0" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        <div className="mt-6 border-t border-white/10 pt-6">
          <div className="mb-3 rounded-2xl border border-white/10 bg-white/10 p-4 shadow-sm">
            <p className="text-xs font-bold uppercase tracking-wide text-brand-redSoft">Current Role</p>
            <p className="mt-1 text-sm font-extrabold text-white">{activeRole.label}</p>
          </div>
          <NavLink
            to="/"
            onClick={onLogout}
            className="flex min-h-11 items-center gap-3 rounded-2xl px-4 py-3 text-sm font-bold text-slate-200 transition duration-200 hover:bg-white/10 hover:text-white focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-white/20"
          >
            <LogOut className="h-5 w-5 shrink-0" />
            <span>Logout</span>
          </NavLink>
        </div>
      </div>
    </aside>
  );
}

export default function Sidebar({ isOpen, onClose }) {
  const { logout } = useAuth();
  const { activeRole } = useRole();
  const items = getRoleNavigation(activeRole.key);

  function handleLogout() {
    logout();
    onClose?.();
  }

  return (
    <>
      <div
        className={`fixed inset-0 z-40 bg-ink-primary/35 backdrop-blur-[2px] transition duration-200 lg:hidden ${isOpen ? 'opacity-100' : 'pointer-events-none opacity-0'}`}
        onClick={onClose}
        aria-hidden="true"
      />

      <SidebarPanel
        items={items}
        activeRole={activeRole}
        onNavigate={onClose}
        onLogout={handleLogout}
        className="fixed left-0 top-0 z-50 hidden h-screen w-[12.5rem] border-r border-white/10 bg-brand-primary shadow-[24px_0_80px_rgba(16,24,40,0.22)] lg:flex xl:w-60"
      />

      <SidebarPanel
        items={items}
        activeRole={activeRole}
        onNavigate={onClose}
        onLogout={handleLogout}
        className={`fixed left-0 top-0 z-50 h-full w-64 border-r border-white/10 bg-brand-primary shadow-[24px_0_80px_rgba(16,24,40,0.22)] transition-transform duration-300 ease-out lg:hidden ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      />
    </>
  );
}

export function MobileNavToggle({ isOpen, onToggle }) {
  return (
    <button
      className="fixed left-4 top-[5.25rem] z-40 grid h-11 w-11 place-items-center rounded-2xl bg-brand-primary text-white shadow-blue transition duration-200 ease-out hover:-translate-y-0.5 hover:bg-brand-secondary active:scale-[0.98] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-brand-redSoft lg:hidden"
      onClick={onToggle}
      aria-label={isOpen ? 'Close navigation' : 'Open navigation'}
      aria-expanded={isOpen}
      type="button"
    >
      {isOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
    </button>
  );
}
