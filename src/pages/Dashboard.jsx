import React from "react";
import { useState } from "react";
import { Area, AreaChart, Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Bell, CalendarClock, CheckCircle2, ClipboardList, Clock, LogOut, Megaphone, Timer } from 'lucide-react';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import ProgressBar from '../components/ui/ProgressBar';
import { activityTimeline, announcements, currentUser, notifications, stats, taskProgress, tasks, weeklyAttendance } from '../data/mockData';
import RoleAccessCard from '../components/RoleAccessCard';
import { Can } from '../components/RoleGate';
import Button from '../components/ui/Button';
import useApiData from '../hooks/useApiData';
import { notificationsApi, pageContent, tasksApi } from '../lib/api';
import { mapNotification, mapTask } from '../lib/mappers';

const icons = [CalendarClock, LogOut, Timer, Clock, CheckCircle2, ClipboardList];
const pieColors = ['#22C55E', '#3B82F6', '#F59E0B'];
const statAccents = [
  'bg-brand-redSoft text-brand-primary',
  'bg-brand-blueAccent text-brand-secondary',
  'bg-brand-successSoft text-emerald-700',
  'bg-brand-warningSoft text-amber-700',
  'bg-brand-blueAccent text-info',
  'bg-brand-infoSoft text-info',
];

export default function Dashboard() {
  const { data: taskItems } = useApiData(
    async () => pageContent(await tasksApi.search({ size: 6 })).map(mapTask),
    tasks,
    [],
  );
  const { data: notificationItems } = useApiData(
    async () => pageContent(await notificationsApi.list({ size: 6 })).map(mapNotification),
    notifications,
    [],
  );

  return (
    <div className="space-y-6">
      <section className="card overflow-hidden bg-gradient-to-r from-white via-brand-blueAccent/50 to-brand-redSoft p-6 shadow-soft">
        <div className="flex flex-col justify-between gap-5 lg:flex-row lg:items-center">
          <div>
            <p className="text-sm font-bold text-brand-primary">Good afternoon, {currentUser.name.split(' ')[0]}</p>
            <h1 className="mt-2 text-3xl font-extrabold text-ink-primary">Your NEXSTAR workspace is ready.</h1>
            <p className="mt-2 max-w-2xl text-ink-secondary">Track attendance, close priority tasks, and keep your people operations moving with a clean daily view.</p>
          </div>
          <div className="rounded-card bg-white px-5 py-4 text-brand-secondary shadow-sm">
            <p className="text-sm font-semibold">Today</p>
            <p className="text-2xl font-extrabold">27 Jun 2026</p>
          </div>
        </div>
      </section>

      <RoleAccessCard />

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {stats.map((stat, index) => {
          const Icon = icons[index];
          return (
            <Card key={stat.label} className="p-5">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm font-semibold text-ink-secondary">{stat.label}</p>
                  <p className="mt-3 text-3xl font-extrabold text-ink-primary">{stat.value}</p>
                  <p className="mt-2 text-xs font-semibold text-brand-primary">{stat.trend}</p>
                </div>
                <div className={`rounded-2xl p-3 ${statAccents[index]}`}>
                  <Icon className="h-5 w-5" />
                </div>
              </div>
            </Card>
          );
        })}
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.3fr_0.7fr]">
        <Card className="p-5">
          <h2 className="text-lg font-extrabold text-ink-primary">Weekly Attendance</h2>
          <div className="mt-4 h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={weeklyAttendance}>
                <defs>
                  <linearGradient id="attendanceRed" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.35} />
                    <stop offset="95%" stopColor="oklch(42.4% 0.199 265.638)" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid stroke="#E5E7EB" vertical={false} />
                <XAxis dataKey="day" stroke="#6B7280" />
                <YAxis stroke="#6B7280" />
                <Tooltip />
                <Area type="monotone" dataKey="present" stroke="#3B82F6" fill="url(#attendanceRed)" strokeWidth={3} />
                <Area type="monotone" dataKey="target" stroke="oklch(42.4% 0.199 265.638)" fill="transparent" strokeDasharray="5 5" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>
        <Card className="p-5">
          <h2 className="text-lg font-extrabold text-ink-primary">Task Progress</h2>
          <div className="mt-4 h-72">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={taskProgress} dataKey="value" innerRadius={62} outerRadius={94} paddingAngle={4}>
                  {taskProgress.map((entry, index) => <Cell key={entry.name} fill={pieColors[index]} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </section>

      <section className="grid gap-6 xl:grid-cols-4">
        <Card className="p-5 xl:col-span-2">
          <h2 className="text-lg font-extrabold text-ink-primary">Today's Tasks</h2>
          <div className="mt-4 space-y-4">
            {taskItems.slice(0, 3).map((task) => (
              <div key={task.id} className="rounded-2xl border border-line p-4">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-bold text-ink-primary">{task.title}</p>
                    <p className="mt-1 text-sm text-ink-secondary">{task.dueDate}</p>
                  </div>
                  <Badge>{task.priority}</Badge>
                </div>
                <div className="mt-3"><ProgressBar value={task.progress} /></div>
              </div>
            ))}
          </div>
        </Card>
        <Card className="p-5">
          <h2 className="flex items-center gap-2 text-lg font-extrabold text-ink-primary"><Bell className="h-5 w-5 text-brand-secondary" /> Recent</h2>
          <div className="mt-4 space-y-4">
            {notificationItems.slice(0, 3).map((note) => (
              <div key={note.id} className="border-b border-line pb-3 last:border-0">
                <p className="text-sm font-bold text-ink-primary">{note.title}</p>
                <p className="mt-1 text-xs text-ink-secondary">{note.time}</p>
              </div>
            ))}
          </div>
        </Card>
        <Card className="p-5">
          <h2 className="flex items-center gap-2 text-lg font-extrabold text-ink-primary"><Megaphone className="h-5 w-5 text-brand-secondary" /> Announcements</h2>
          <div className="mt-4 space-y-3">
            {announcements.map((item) => <p key={item} className="rounded-2xl bg-brand-blueAccent p-3 text-sm font-medium text-brand-secondary">{item}</p>)}
          </div>
          <Can roles={['admin']}>
            <Button className="mt-4 w-full" variant="secondary">Manage Announcements</Button>
          </Can>
        </Card>
      </section>

      <Card className="p-5">
        <h2 className="text-lg font-extrabold text-ink-primary">Activity Timeline</h2>
        <div className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {activityTimeline.map((item) => (
            <div key={item.title} className="rounded-2xl border border-line p-4">
              <p className="text-sm font-bold text-brand-secondary">{item.time}</p>
              <p className="mt-2 font-bold text-ink-primary">{item.title}</p>
              <p className="mt-1 text-sm text-ink-secondary">{item.detail}</p>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
