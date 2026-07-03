import React, { useState } from 'react';
import { Activity, BarChart3, Megaphone, Plus, ShieldCheck, Trash2, UserCog, Users } from 'lucide-react';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import useApiData from '../hooks/useApiData';
import { adminApi, announcementsApi, pageContent } from '../lib/api';

const adminPowers = [
  { title: 'Complete System Access', detail: 'Full visibility across employees, tasks, attendance, reports, and settings.', icon: ShieldCheck },
  { title: 'Add, Edit & Delete Employees', detail: 'Manage employee records, updates, and termination controls.', icon: Users },
  { title: 'Create Departments', detail: 'Set up company departments for HR and reporting structure.', icon: Plus },
  { title: 'Assign Tasks', detail: 'Create work items and assign them to employees or teams.', icon: UserCog },
  { title: 'Monitor Employee Activities', detail: 'Review check-ins, task updates, reports, and timeline activity.', icon: Activity },
  { title: 'View Reports', detail: 'Open attendance, employee, project, and work performance reports.', icon: BarChart3 },
  { title: 'Manage Announcements', detail: 'Create and publish company announcements.', icon: Megaphone },
];

const adminAccentClasses = [
  'bg-brand-redSoft text-brand-primary',
  'bg-brand-blueAccent text-brand-secondary',
  'bg-brand-infoSoft text-info',
  'bg-brand-successSoft text-emerald-700',
  'bg-brand-warningSoft text-amber-700',
  'bg-brand-blueAccent text-brand-secondary',
];

export default function AdminConsole() {
  const [showAnnouncementForm, setShowAnnouncementForm] = useState(false);
  const [editingAnnouncement, setEditingAnnouncement] = useState(null);
  const [announcementStatus, setAnnouncementStatus] = useState('');
  const [announcementForm, setAnnouncementForm] = useState({
    title: '',
    message: '',
    audienceRole: '',
  });
  const { data: announcements, loading, error, refresh } = useApiData(
    () => announcementsApi.list({ includeInactive: true }),
    [],
    [],
  );
  const { data: activityLogs, loading: activityLoading, error: activityError, refresh: refreshActivity } = useApiData(
    () => adminApi.auditLogs({ page: 0, size: 10 }),
    { content: [] },
    [],
  );
  const activityItems = pageContent(activityLogs);

  function startAnnouncementCreate() {
    setEditingAnnouncement(null);
    setAnnouncementForm({ title: '', message: '', audienceRole: '' });
    setShowAnnouncementForm((value) => !value);
  }

  function startAnnouncementEdit(item) {
    setEditingAnnouncement(item);
    setAnnouncementForm({
      title: item.title || '',
      message: item.message || '',
      audienceRole: item.audienceRole || '',
    });
    setShowAnnouncementForm(true);
  }

  async function submitAnnouncement(event) {
    event.preventDefault();
    setAnnouncementStatus(editingAnnouncement ? 'Updating announcement...' : 'Creating announcement...');
    try {
      if (editingAnnouncement) {
        await announcementsApi.update(editingAnnouncement.id, { ...announcementForm, active: true });
      } else {
        await announcementsApi.create(announcementForm);
      }
      setAnnouncementStatus(editingAnnouncement ? 'Announcement updated.' : 'Announcement created.');
      setShowAnnouncementForm(false);
      setEditingAnnouncement(null);
      refresh();
    } catch (err) {
      setAnnouncementStatus(err.message);
    }
  }

  async function deleteAnnouncement(id) {
    setAnnouncementStatus('Deleting announcement...');
    try {
      await announcementsApi.remove(id);
      setAnnouncementStatus('Announcement deleted.');
      refresh();
    } catch (err) {
      setAnnouncementStatus(err.message);
    }
  }

  return (
    <div className="page-stack">
      <div className="page-header">
        <p className="page-kicker">Admin Console</p>
        <h1 className="page-title">Complete system access</h1>
      </div>

      <section className="card-grid md:grid-cols-2 xl:grid-cols-3">
        {adminPowers.map((power, index) => {
          const Icon = power.icon;
          return (
            <Card key={power.title} className="p-5">
              <div className={`stat-icon-wrap h-12 w-12 ${adminAccentClasses[index % adminAccentClasses.length]}`}>
                <Icon className="h-5 w-5" />
              </div>
              <h2 className="section-title mt-4">{power.title}</h2>
              <p className="mt-2 text-sm leading-6 text-ink-secondary">{power.detail}</p>
            </Card>
          );
        })}
      </section>

      <section className="section-grid xl:grid-cols-2">
        <Card className="p-5" interactive={false}>
          <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
            <h2 className="section-title">Employee Activity Monitor</h2>
            <Button variant="blue" onClick={() => refreshActivity()}><Activity className="h-4 w-4" /> Refresh Activity</Button>
          </div>
          {activityError && <p className="alert-warning mt-4" role="alert">{activityError}</p>}
          <div className="mt-5 space-y-3">
            {activityLoading && <p className="text-sm text-ink-secondary">Loading activity from audit logs...</p>}
            {!activityLoading && activityItems.length === 0 && <p className="text-sm text-ink-secondary">No recent activity recorded.</p>}
            {activityItems.map((item) => (
              <div key={item.id} className="rounded-2xl border border-line p-4">
                <p className="text-sm font-bold text-brand-secondary">{item.createdAt || item.timestamp || 'Recent'}</p>
                <p className="mt-1 font-bold text-ink-primary">{item.action || 'System activity'}</p>
                <p className="mt-1 text-sm text-ink-secondary">{item.entityType ? `${item.entityType}${item.userId || item.performedBy ? ` · ${item.userId || item.performedBy}` : ''}` : item.detail || 'Activity logged'}</p>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-5" interactive={false}>
          <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
            <h2 className="section-title">Announcement Manager</h2>
            <Button onClick={startAnnouncementCreate}><Megaphone className="h-4 w-4" /> New Announcement</Button>
          </div>
          {announcementStatus && <p className="alert-info mt-4" role="status">{announcementStatus}</p>}
          {error && <p className="alert-warning mt-4" role="alert">{error}</p>}
          {showAnnouncementForm && (
            <form className="mt-5 grid gap-4" onSubmit={submitAnnouncement}>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Title</span>
                <input className="field-control mt-2" value={announcementForm.title} onChange={(event) => setAnnouncementForm((current) => ({ ...current, title: event.target.value }))} required />
              </label>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Message</span>
                <textarea className="field-control mt-2 min-h-24" value={announcementForm.message} onChange={(event) => setAnnouncementForm((current) => ({ ...current, message: event.target.value }))} required />
              </label>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Audience Role</span>
                <select className="select-control mt-2" value={announcementForm.audienceRole} onChange={(event) => setAnnouncementForm((current) => ({ ...current, audienceRole: event.target.value }))}>
                  <option value="">All roles</option>
                  <option>SUPER_ADMIN</option>
                  <option>HR</option>
                  <option>MANAGER</option>
                  <option>TEAM_LEAD</option>
                  <option>EMPLOYEE</option>
                </select>
              </label>
              <div className="flex gap-3">
                <Button type="submit">{editingAnnouncement ? 'Update Announcement' : 'Create Announcement'}</Button>
                <Button type="button" variant="secondary" onClick={() => setShowAnnouncementForm(false)}>Cancel</Button>
              </div>
            </form>
          )}
          <div className="mt-5 space-y-3">
            {loading && <p className="text-sm text-ink-secondary">Loading announcements...</p>}
            {announcements.length === 0 && !loading && <p className="text-sm text-ink-secondary">No announcements yet.</p>}
            {announcements.map((item) => (
              <div key={item.id} className="flex items-center justify-between gap-4 rounded-2xl bg-brand-blueAccent p-4">
                <div>
                  <p className="text-sm font-semibold text-brand-secondary">{item.title}</p>
                  <p className="mt-1 text-xs text-ink-secondary">{item.message}</p>
                </div>
                <div className="flex gap-2">
                  <Button variant="secondary" onClick={() => startAnnouncementEdit(item)}>Edit</Button>
                  <Button variant="secondary" onClick={() => deleteAnnouncement(item.id)} aria-label={`Delete ${item.title}`}><Trash2 className="h-4 w-4" /></Button>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </section>
    </div>
  );
}
