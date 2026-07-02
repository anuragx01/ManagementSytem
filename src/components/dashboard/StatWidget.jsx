import React from 'react';
import Card from '../ui/Card';

export default function StatWidget({ label, value, icon: Icon, accent = 'bg-brand-blueAccent text-brand-secondary' }) {
  return (
    <Card className="p-5 transition duration-200 hover:-translate-y-0.5 hover:shadow-soft" interactive={false}>
      <div className="flex items-center justify-between gap-3">
        <div className="min-w-0">
          <p className="text-sm font-semibold text-ink-secondary">{label}</p>
          <p className="mt-3 truncate text-2xl font-extrabold text-ink-primary sm:text-3xl">{value ?? '—'}</p>
        </div>
        {Icon && (
          <div className={`stat-icon-wrap shrink-0 ${accent}`}>
            <Icon className="h-5 w-5" />
          </div>
        )}
      </div>
    </Card>
  );
}
