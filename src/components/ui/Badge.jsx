import React from "react";
const styles = {
  High: 'bg-brand-redSoft text-brand-primary',
  Medium: 'bg-brand-warningSoft text-amber-700',
  Low: 'bg-brand-successSoft text-emerald-700',
  Present: 'bg-brand-successSoft text-emerald-700',
  Late: 'bg-brand-warningSoft text-amber-700',
  Leave: 'bg-brand-blueAccent text-info',
  Completed: 'bg-brand-successSoft text-emerald-700',
  'In Progress': 'bg-brand-blueAccent text-brand-secondary',
  Pending: 'bg-brand-warningSoft text-amber-700',
  Review: 'bg-brand-blueAccent text-info',
  Available: 'bg-brand-successSoft text-emerald-700',
  'In Meeting': 'bg-brand-warningSoft text-amber-700',
  Away: 'bg-slate-100 text-slate-600',
  Focus: 'bg-brand-infoSoft text-info',
};

export default function Badge({ children }) {
  return <span className={`chip ${styles[children] || 'bg-brand-infoSoft text-info'}`}>{children}</span>;
}
