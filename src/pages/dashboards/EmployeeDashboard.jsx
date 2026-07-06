import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Bell, ClipboardList, Clock, LogIn, LogOut, Megaphone } from 'lucide-react';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import PageHeader from '../../components/ui/PageHeader';
import StatWidget from '../../components/dashboard/StatWidget';
import { LoadingIndicator } from '../../components/ui/Skeleton';
import useApiData from '../../hooks/useApiData';
import { buildWelcomeTitle } from '../../lib/greeting';
import {
  attendanceApi,
  employeesApi,
  notificationsApi,
  pageContent,
  tasksApi,
} from '../../lib/api';
import { mapNotification, mapTask } from '../../lib/mappers';


function formatTime(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('en-GB', { hour: '2-digit', minute: '2-digit' }).format(new Date(value));
}

export default function EmployeeDashboard() {
  const { data: profile } = useApiData(() => employeesApi.me(), null, []);
  const { data: today, loading: todayLoading, error: todayError, refresh: refreshToday } = useApiData(
    () => attendanceApi.today(),
    null,
    [],
  );
  const { data: tasks, loading: tasksLoading } = useApiData(
    async () => {
      if (!profile?.id) return [];
      return pageContent(await tasksApi.search({ assigneeId: profile.id, size: 20 })).map(mapTask);
    },
    [],
    [profile?.id],
  );
  const { data: notifications } = useApiData(
    async () => pageContent(await notificationsApi.list({ size: 6 })).map(mapNotification),
    [],
    [],
  );

  const pendingTasks = tasks.filter((task) => !['DONE', 'Completed'].includes(task.status));
  const completedTasks = tasks.filter((task) => ['DONE', 'Completed'].includes(task.status));
 const companyAnnouncements = useMemo(() => {
  return (notifications || []).filter((note) =>
    /announcement|system/i.test(note.type || note.raw?.type || '')
  );
}, [notifications]);

  async function handleClockIn() {
    await attendanceApi.clockIn({});
    refreshToday();
  }

  async function handleClockOut() {
    await attendanceApi.clockOut({});
    refreshToday();
  }

  return (
    <div className="page-stack">
      <PageHeader
        kicker="Employee Portal"
        title={buildWelcomeTitle(profile?.firstName || 'Employee')}
        description="Welcome back! Here's what's happening across NexStar today."
        actions={
          <>
            <Button onClick={handleClockIn} disabled={Boolean(today?.clockIn && !today?.clockOut)}>
              <LogIn className="h-4 w-4" /> Check In
            </Button>
            <Button variant="secondary" onClick={handleClockOut} disabled={!today?.clockIn || Boolean(today?.clockOut)}>
              <LogOut className="h-4 w-4" /> Check Out
            </Button>
          </>
        }
      />

      {todayError && <p className="alert-warning" role="alert">{todayError}</p>}
      {todayLoading && <LoadingIndicator message="Loading today&apos;s attendance..." />}

      <section className="card-grid sm:grid-cols-2 xl:grid-cols-3">
        <StatWidget label="Today's Login Time" value={formatTime(today?.clockIn)} icon={LogIn} />
        <StatWidget label="Today's Logout Time" value={formatTime(today?.clockOut)} icon={LogOut} />
        <StatWidget label="Total Working Hours" value={today?.workedHours || '—'} icon={Clock} accent="bg-brand-successSoft text-emerald-700" />
        <StatWidget label="Assigned Tasks" value={tasksLoading ? '—' : tasks.length} icon={ClipboardList} />
        <StatWidget label="Pending Tasks" value={tasksLoading ? '—' : pendingTasks.length} icon={ClipboardList} accent="bg-brand-warningSoft text-amber-700" />
        <StatWidget label="Completed Tasks" value={tasksLoading ? '—' : completedTasks.length} icon={ClipboardList} accent="bg-brand-successSoft text-emerald-700" />
      </section>

      <section className="section-grid xl:grid-cols-2">
        <Card className="p-5" interactive={false}>
          <div className="flex items-center justify-between gap-3">
            <h2 className="section-title flex items-center gap-2"><Bell className="h-5 w-5 text-brand-secondary" /> Recent Notifications</h2>
            <Link to="/notifications" className="text-sm font-bold text-brand-primary hover:underline">View all</Link>
          </div>
          <div className="mt-4 space-y-3">
            {notifications.length === 0 && <p className="text-sm text-ink-secondary">No notifications yet.</p>}
            {notifications.slice(0, 5).map((note) => (
              <div key={note.id} className="rounded-2xl border border-line px-4 py-3">
                <p className="text-sm font-bold text-ink-primary">{note.title}</p>
                <p className="mt-1 text-xs text-ink-secondary">{note.message || note.time}</p>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><Megaphone className="h-5 w-5 text-brand-primary" /> Company Announcements</h2>
          <div className="mt-4 space-y-3">
            {companyAnnouncements.slice(0, 4).map((item) => (
              <div key={item.id} className="rounded-2xl bg-brand-blueAccent px-4 py-3">
                <p className="text-sm font-bold text-ink-primary">{item.title || 'Announcement'}</p>
                <p className="mt-1 text-sm text-ink-secondary">{item.message}</p>
              </div>
            ))}
          </div>
        </Card>
      </section>

      <Card className="p-5" interactive={false}>
        <h2 className="section-title">Quick Actions</h2>
        <div className="mt-4 flex flex-wrap gap-3">
          <Link to="/attendance"><Button variant="secondary">My Attendance</Button></Link>
          <Link to="/tasks"><Button variant="secondary">My Tasks</Button></Link>
          <Link to="/daily-reports"><Button variant="secondary">Daily Work Report</Button></Link>
          <Link to="/team"><Button variant="secondary">Team Directory</Button></Link>
        </div>
      </Card>
    </div>
  );
}

