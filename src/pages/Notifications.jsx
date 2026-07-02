import React, { useState } from "react";
import { AlarmClock, Bell, ClipboardPlus, Megaphone } from 'lucide-react';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import { LoadingIndicator } from '../components/ui/Skeleton';
import useApiData from '../hooks/useApiData';
import { notificationsApi, pageContent } from '../lib/api';
import { mapNotification } from '../lib/mappers';

const meta = {
  'New Task': { icon: ClipboardPlus, className: 'bg-brand-blueAccent text-brand-secondary' },
  'Task Assigned': { icon: ClipboardPlus, className: 'bg-brand-blueAccent text-brand-secondary' },
  Deadline: { icon: AlarmClock, className: 'bg-brand-warningSoft text-amber-700' },
  Announcement: { icon: Megaphone, className: 'bg-brand-blueAccent text-brand-secondary' },
  Reminder: { icon: Bell, className: 'bg-brand-infoSoft text-info' },
};

export default function Notifications() {
  const [actionStatus, setActionStatus] = useState('');
  const { data: notificationItems, loading, error, refresh } = useApiData(
    async () => pageContent(await notificationsApi.list({ size: 30 })).map(mapNotification),
    [],
    [],
  );

  async function markAllRead() {
    setActionStatus('Marking all as read...');
    try {
      await notificationsApi.readAll();
      refresh();
      setActionStatus('All notifications marked as read.');
    } catch (err) {
      setActionStatus(err.message);
    }
  }

  async function markRead(id) {
    try {
      await notificationsApi.markRead(id);
      refresh();
    } catch (err) {
      setActionStatus(err.message);
    }
  }

  return (
    <div className="page-stack">
      <div className="page-header flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="page-kicker">Notifications</p>
          <h1 className="page-title">Updates and reminders</h1>
        </div>
        <Button variant="secondary" onClick={markAllRead}>Mark all read</Button>
      </div>
      {error && <p className="alert-warning" role="alert">{error}</p>}
      {actionStatus && <p className="alert-info" role="status">{actionStatus}</p>}
      {loading && <LoadingIndicator message="Loading notifications from backend..." />}
      <section className="space-y-4">
        {notificationItems.length === 0 && !loading && (
          <Card className="p-5" interactive={false}>
            <p className="text-sm text-ink-secondary">No notifications yet.</p>
          </Card>
        )}
        {notificationItems.map((note) => {
          const noteMeta = meta[note.type] || meta.Reminder;
          const Icon = noteMeta.icon;
          return (
            <Card key={note.id} className="p-5">
              <div className="flex gap-4">
                <div className={`stat-icon-wrap h-12 w-12 ${noteMeta.className}`}>
                  <Icon className="h-5 w-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-xs font-bold uppercase text-brand-primary">{note.type}</p>
                      <h2 className="section-title mt-1">{note.title}</h2>
                    </div>
                    {note.unread && (
                      <button
                        type="button"
                        className="mt-2 h-3 w-3 rounded-full bg-brand-accent"
                        aria-label="Mark as read"
                        onClick={() => markRead(note.id)}
                      />
                    )}
                  </div>
                  <p className="mt-2 text-sm text-ink-secondary">{note.message}</p>
                  <p className="mt-3 text-xs font-semibold text-ink-secondary">{note.time}</p>
                </div>
              </div>
            </Card>
          );
        })}
      </section>
    </div>
  );
}
