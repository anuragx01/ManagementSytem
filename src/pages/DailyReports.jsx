import React, { useEffect, useMemo, useState } from 'react';
import { FileText, History, Save } from 'lucide-react';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import PageHeader from '../components/ui/PageHeader';
import { EmptyState, LoadingIndicator } from '../components/ui/Skeleton';
import useApiData from '../hooks/useApiData';
import { employeesApi } from '../lib/api';
import { listDailyReportHistory, loadDailyReport, saveDailyReport, todayKey } from '../lib/dailyReportStorage';

const emptyForm = {
  completedWork: '',
  pendingWork: '',
  tomorrowPlan: '',
  blockers: '',
  status: 'draft',
};

export default function DailyReports() {
  const { data: profile, loading } = useApiData(() => employeesApi.me(), null, []);
  const [form, setForm] = useState(emptyForm);
  const [statusMessage, setStatusMessage] = useState('');
  const [history, setHistory] = useState([]);

  useEffect(() => {
    if (!profile?.userId && !profile?.id) return;
    const userKey = profile.userId || profile.id;
    const saved = loadDailyReport(userKey);
    if (saved) setForm(saved);
    setHistory(listDailyReportHistory(userKey));
  }, [profile?.userId, profile?.id]);

  const canEdit = form.status !== 'submitted';
  const userKey = profile?.userId || profile?.id;

  function persist(nextForm) {
    if (!userKey) return;
    saveDailyReport(userKey, nextForm);
    setHistory(listDailyReportHistory(userKey));
  }

  function saveDraft() {
    const next = { ...form, status: 'draft' };
    setForm(next);
    persist(next);
    setStatusMessage('Draft saved locally.');
  }

  function submitReport(event) {
    event.preventDefault();
    if (!canEdit) return;
    const next = { ...form, status: 'submitted', submittedAt: new Date().toISOString() };
    setForm(next);
    persist(next);
    setStatusMessage('Daily work report submitted.');
  }

  const todayLabel = useMemo(() => new Intl.DateTimeFormat('en-GB', { dateStyle: 'full' }).format(new Date()), []);

  return (
    <div className="page-stack">
      <PageHeader
        kicker="Daily Work Report"
        title="End-of-day summary"
        description={`Submit your completed work, pending items, tomorrow's plan, and blockers for ${todayLabel}.`}
      />

      {loading && <LoadingIndicator message="Loading profile..." />}
      {statusMessage && <p className="alert-success" role="status">{statusMessage}</p>}

      <Card className="p-5" interactive={false}>
        <form className="grid gap-4" onSubmit={submitReport}>
          {[
            ['completedWork', 'Completed Work'],
            ['pendingWork', 'Pending Work'],
            ['tomorrowPlan', "Tomorrow's Plan"],
            ['blockers', 'Issues / Blockers'],
          ].map(([key, label]) => (
            <label key={key} className="block">
              <span className="text-sm font-semibold text-ink-primary">{label}</span>
              <textarea
                className="field-control mt-2 min-h-24"
                value={form[key]}
                onChange={(event) => setForm((current) => ({ ...current, [key]: event.target.value }))}
                disabled={!canEdit}
                required={key === 'completedWork'}
              />
            </label>
          ))}
          <div className="flex flex-wrap gap-3">
            <Button type="button" variant="secondary" onClick={saveDraft} disabled={!canEdit}>
              <Save className="h-4 w-4" /> Save Draft
            </Button>
            <Button type="submit" disabled={!canEdit}>
              <FileText className="h-4 w-4" /> Submit Report
            </Button>
          </div>
          {!canEdit && (
            <p className="text-sm text-ink-secondary">Report submitted for {todayKey()}. Editing is locked until backend submission APIs are available.</p>
          )}
        </form>
      </Card>

      <Card className="p-5" interactive={false}>
        <h2 className="section-title flex items-center gap-2"><History className="h-5 w-5" /> Submission History</h2>
        <div className="mt-4 space-y-3">
          {history.length === 0 && <EmptyState title="No reports yet" message="Saved drafts and submitted reports will appear here." />}
          {history.map((entry) => (
            <div key={entry.date} className="rounded-2xl border border-line px-4 py-3">
              <div className="flex items-center justify-between gap-3">
                <p className="text-sm font-bold text-ink-primary">{entry.date}</p>
                <span className="text-xs font-bold uppercase text-brand-primary">{entry.status || 'draft'}</span>
              </div>
              <p className="mt-2 line-clamp-2 text-sm text-ink-secondary">{entry.completedWork || 'No summary saved.'}</p>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
