import React from 'react';
import { ShieldCheck } from 'lucide-react';
import { useRole } from '../context/RoleContext';
import Card from './ui/Card';

export default function RoleAccessCard() {
  const { activeRole } = useRole();
  const accents = [
    'bg-brand-redSoft text-brand-primary',
    'bg-brand-blueAccent text-brand-secondary',
    'bg-brand-infoSoft text-info',
    'bg-brand-successSoft text-emerald-700',
    'bg-brand-warningSoft text-amber-700',
    'bg-brand-blueAccent text-brand-secondary',
  ];

  return (
    <Card className="p-5" interactive={false}>
      <div className="flex flex-col justify-between gap-6 lg:flex-row lg:items-start">
        <div>
          <div className="flex items-center gap-2 text-sm font-bold text-brand-primary">
            <ShieldCheck className="h-4 w-4 shrink-0" aria-hidden="true" />
            Active role: {activeRole.label}
          </div>
          <h2 className="section-title mt-2">{activeRole.title}</h2>
          <p className="mt-2 text-sm leading-6 text-ink-secondary">This is static frontend permission logic for UI preview.</p>
        </div>
        <div className="card-grid sm:grid-cols-2 lg:min-w-[520px]">
          {activeRole.permissions.map((permission, index) => (
            <div key={permission} className={`rounded-2xl px-4 py-3 text-sm font-semibold ${accents[index % accents.length]}`}>
              {permission}
            </div>
          ))}
        </div>
      </div>
    </Card>
  );
}
