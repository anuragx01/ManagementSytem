import React from "react";
import { Loader2 } from 'lucide-react';

export function SkeletonCard() {
  return (
    <div className="card animate-pulse p-5">
      <div className="h-4 w-28 rounded bg-slate-100" />
      <div className="mt-5 h-8 w-40 rounded bg-slate-100" />
      <div className="mt-4 h-3 w-full rounded bg-slate-100" />
      <div className="mt-2 h-3 w-3/4 rounded bg-slate-100" />
    </div>
  );
}

export function EmptyState({ title = 'Nothing here yet', message = 'Items will appear here once available.' }) {
  return (
    <div className="card flex min-h-48 flex-col items-center justify-center p-8 text-center">
      <div className="mb-4 grid h-12 w-12 place-items-center rounded-2xl bg-brand-blueAccent">
        <span className="h-5 w-5 rounded-full bg-brand-primary/20" aria-hidden="true" />
      </div>
      <h3 className="text-base font-bold text-ink-primary">{title}</h3>
      <p className="mt-2 max-w-sm text-sm text-ink-secondary">{message}</p>
    </div>
  );
}

export function LoadingIndicator({ message = 'Loading...' }) {
  return (
    <p className="loading-text" role="status" aria-live="polite">
      <Loader2 className="h-4 w-4 animate-spin text-brand-primary" aria-hidden="true" />
      {message}
    </p>
  );
}
