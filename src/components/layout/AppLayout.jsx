import React from "react";
import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar, { MobileNavToggle } from './Sidebar';
import Navbar from './Navbar';
import Toast from '../ui/Toast';

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="min-h-screen bg-surface-muted lg:pl-[12.5rem] xl:pl-60">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="min-h-screen">
        <Navbar />
        <MobileNavToggle isOpen={sidebarOpen} onToggle={() => setSidebarOpen((value) => !value)} />
        <main className="mx-auto max-w-7xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
          <Outlet />
        </main>
      </div>
      <Toast />
    </div>
  );
}

