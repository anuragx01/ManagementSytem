import React from "react";
import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Menu, X } from 'lucide-react';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import Toast from '../ui/Toast';

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="min-h-screen bg-surface-muted">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div>
        <Navbar />
        <button
          className="fixed left-5 top-18 z-40 grid h-12 w-12 place-items-center rounded-2xl bg-brand-primary text-white shadow-soft transition hover:bg-brand-secondary"
          onClick={() => setSidebarOpen((value) => !value)}
          aria-label={sidebarOpen ? 'Close navigation' : 'Open navigation'}
          aria-expanded={sidebarOpen}
          type="button"
        >
          {sidebarOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </button>
        <main className="mx-auto max-w-7xl px-5 py-6 lg:px-8">
          <Outlet />
        </main>
      </div>
      <Toast />
    </div>
  );
}
