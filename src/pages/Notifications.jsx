import React from "react";
import { AlarmClock, Bell, ClipboardPlus, Megaphone } from 'lucide-react';
import Card from '../components/ui/Card';
import { notifications } from '../data/mockData';
import useApiData from '../hooks/useApiData';
import { notificationsApi, pageContent } from '../lib/api';
import { mapNotification } from '../lib/mappers';

const meta = {
  'New Task': { icon: ClipboardPlus, className: 'bg-brand-blueAccent text-brand-secondary' },
  Deadline: { icon: AlarmClock, className: 'bg-brand-warningSoft text-amber-700' },
  Announcement: { icon: Megaphone, className: 'bg-brand-blueAccent text-brand-secondary' },
  Reminder: { icon: Bell, className: 'bg-brand-infoSoft text-info' },
};

export default function Notifications() {
  const { data: notificationItems, loading, error } = useApiData(
    async () => pageContent(await notificationsApi.list({ size: 30 })).map(mapNotification),
    notifications,
    [],
  );

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-bold text-brand-primary">Notifications</p>
        <h1 className="mt-1 text-3xl font-extrabold text-ink-primary">Updates and reminders</h1>
      </div>
      {error && <p className="rounded-2xl bg-orange-50 px-4 py-3 text-sm font-semibold text-orange-700">Showing mock notifications because API is unavailable: {error}</p>}
      {loading && <p className="text-sm font-semibold text-ink-secondary">Loading notifications from backend...</p>}
      <section className="space-y-4">
        {notificationItems.map((note) => {
          const noteMeta = meta[note.type] || meta.Reminder;
          const Icon = noteMeta.icon;
          return (
            <Card key={note.id} className="p-5">
              <div className="flex gap-4">
                <div className={`grid h-12 w-12 shrink-0 place-items-center rounded-2xl ${noteMeta.className}`}>
                  <Icon className="h-5 w-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-xs font-bold uppercase text-brand-primary">{note.type}</p>
                      <h2 className="mt-1 text-lg font-extrabold text-ink-primary">{note.title}</h2>
                    </div>
                    {note.unread && <span className="mt-2 h-3 w-3 rounded-full bg-brand-primary" />}
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
