import React from "react";
import { useState } from 'react';
import { CheckCircle2, FileText, Plus } from 'lucide-react';
import { Can } from '../components/RoleGate';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import { EmptyState, SkeletonCard } from '../components/ui/Skeleton';
import { reportEntries } from '../data/mockData';

export default function DailyReports() {
  const [showReportForm, setShowReportForm] = useState(false);
  const [reportStatus, setReportStatus] = useState('');

  function submitReport(event) {
    event.preventDefault();
    setReportStatus('Daily work report submitted.');
    setShowReportForm(false);
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <p className="text-sm font-bold text-brand-primary">Daily Work Report</p>
          <h1 className="mt-1 text-3xl font-extrabold text-ink-primary">Today’s summary</h1>
        </div>
        <Can roles={['employee', 'admin']}>
          <Button onClick={() => setShowReportForm((value) => !value)}><Plus className="h-4 w-4" /> New Report</Button>
        </Can>
      </div>
      {reportStatus && <p className="rounded-2xl bg-brand-successSoft px-4 py-3 text-sm font-semibold text-emerald-700">{reportStatus}</p>}
      {showReportForm && (
        <Can roles={['employee', 'admin']}>
          <Card className="p-5">
            <h2 className="text-lg font-extrabold text-ink-primary">Submit Daily Work Report</h2>
            <form className="mt-5 grid gap-4" onSubmit={submitReport}>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Work Summary</span>
                <textarea className="mt-2 min-h-28 w-full rounded-2xl border border-line px-4 py-3 outline-none focus:border-brand-primary focus:ring-4 focus:ring-red-100" required />
              </label>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Blockers</span>
                <textarea className="mt-2 min-h-20 w-full rounded-2xl border border-line px-4 py-3 outline-none focus:border-brand-primary focus:ring-4 focus:ring-red-100" />
              </label>
              <Button type="submit">Submit Report</Button>
            </form>
          </Card>
        </Can>
      )}
      <Can roles={['admin', 'manager']}>
        <Card className="flex flex-col justify-between gap-4 p-5 sm:flex-row sm:items-center">
          <div>
            <h2 className="text-lg font-extrabold text-ink-primary">Review queue</h2>
            <p className="mt-1 text-sm text-ink-secondary">Managers can review completed work and track employee progress.</p>
          </div>
          <Button variant="secondary"><CheckCircle2 className="h-4 w-4" /> Review Reports</Button>
        </Card>
      </Can>
      <section className="grid gap-5 lg:grid-cols-3">
        {reportEntries.map((entry) => (
          <Card key={entry.area} className="p-5">
            <FileText className="h-6 w-6 text-brand-primary" />
            <h2 className="mt-4 text-xl font-extrabold text-ink-primary">{entry.area}</h2>
            <p className="mt-2 min-h-16 text-sm leading-6 text-ink-secondary">{entry.work}</p>
            <div className="mt-5 flex items-center justify-between">
              <span className="text-sm font-bold text-ink-primary">{entry.hours}</span>
              <Badge>{entry.status}</Badge>
            </div>
          </Card>
        ))}
      </section>
      <section className="grid gap-5 lg:grid-cols-2">
        <SkeletonCard />
        <EmptyState title="No blockers reported" message="Daily blockers and manager comments will appear here." />
      </section>
    </div>
  );
}
