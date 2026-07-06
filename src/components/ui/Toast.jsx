import React from "react";
import { CheckCircle2, X } from 'lucide-react';

export default function Toast() {
  return (
    <div className="fixed bottom-5 right-5 z-50 hidden max-w-sm items-center gap-3 rounded-2xl border border-line bg-white p-4 shadow-soft md:flex">
      <CheckCircle2 className="h-5 w-5 text-success" />
      <div>
        <p className="text-sm font-semibold text-ink-primary">NEXSTAR synced</p>
        <p className="text-xs text-ink-secondary">Static UI preview is ready.</p>
      </div>
      <X className="ml-2 h-4 w-4 text-ink-secondary" />
    </div>
  );
}

