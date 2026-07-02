import React from 'react';
import { Activity, BarChart3, Megaphone, Plus, ShieldCheck, UserCog, Users } from 'lucide-react';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import { activityTimeline, announcements } from '../data/mockData';

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
            <Button variant="blue"><Activity className="h-4 w-4" /> Monitor</Button>
          </div>
          <div className="mt-5 space-y-3">
            {activityTimeline.map((item) => (
              <div key={item.title} className="rounded-2xl border border-line p-4">
                <p className="text-sm font-bold text-brand-secondary">{item.time}</p>
                <p className="mt-1 font-bold text-ink-primary">{item.title}</p>
                <p className="mt-1 text-sm text-ink-secondary">{item.detail}</p>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-5" interactive={false}>
          <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
            <h2 className="section-title">Announcement Manager</h2>
            <Button><Megaphone className="h-4 w-4" /> New Announcement</Button>
          </div>
          <div className="mt-5 space-y-3">
            {announcements.map((item) => (
              <div key={item} className="flex items-center justify-between gap-4 rounded-2xl bg-brand-blueAccent p-4">
                <p className="text-sm font-semibold text-brand-secondary">{item}</p>
                <Button variant="secondary">Edit</Button>
              </div>
            ))}
          </div>
        </Card>
      </section>
    </div>
  );
}
