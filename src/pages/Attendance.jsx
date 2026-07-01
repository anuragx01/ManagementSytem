import React from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Clock, LogIn, LogOut } from 'lucide-react';
import { Can } from '../components/RoleGate';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import Card from '../components/ui/Card';
import { attendanceRows, calendarDays, weeklyAttendance } from '../data/mockData';
import useApiData from '../hooks/useApiData';
import { attendanceApi, pageContent } from '../lib/api';
import { mapAttendance } from '../lib/mappers';

const summary = [
  { label: 'Check In', value: '09:18 AM', icon: LogIn },
  { label: 'Check Out', value: '06:12 PM', icon: LogOut },
  { label: "Today's Working Hours", value: '7h 44m', icon: Clock },
];

export default function Attendance() {
  const { data: rows, loading, error } = useApiData(
    async () => pageContent(await attendanceApi.my({ size: 30 })).map(mapAttendance),
    attendanceRows,
    [],
  );

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-bold text-brand-primary">Attendance</p>
        <h1 className="mt-1 text-3xl font-extrabold text-ink-primary">Daily time overview</h1>
      </div>
      <section className="grid gap-4 md:grid-cols-3">
        {summary.map((item) => {
          const Icon = item.icon;
          return (
            <Card key={item.label} className="p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-semibold text-ink-secondary">{item.label}</p>
                  <p className="mt-3 text-3xl font-extrabold text-ink-primary">{item.value}</p>
                </div>
                <div className="rounded-2xl bg-brand-blueAccent p-3 text-brand-secondary"><Icon className="h-5 w-5" /></div>
              </div>
            </Card>
          );
        })}
      </section>
      {error && <p className="rounded-2xl bg-orange-50 px-4 py-3 text-sm font-semibold text-orange-700">Showing mock attendance because API is unavailable: {error}</p>}
      {loading && <p className="text-sm font-semibold text-ink-secondary">Loading attendance from backend...</p>}
      <Can roles={['admin', 'hr']}>
        <Card className="flex flex-col justify-between gap-4 p-5 sm:flex-row sm:items-center">
          <div>
            <h2 className="text-lg font-extrabold text-ink-primary">Attendance administration</h2>
            <p className="mt-1 text-sm text-ink-secondary">HR and Admin can view attendance records and update exceptions.</p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button>Export Report</Button>
            <Button variant="secondary">Manage Exceptions</Button>
          </div>
        </Card>
      </Can>
      <section className="grid gap-6 xl:grid-cols-[1fr_0.8fr]">
        <Card className="overflow-hidden">
          <div className="border-b border-line p-5">
            <h2 className="text-lg font-extrabold text-ink-primary">Attendance Table</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface-muted text-xs uppercase text-ink-secondary">
                <tr>
                  {['Date', 'Check In', 'Check Out', 'Hours', 'Status'].map((head) => <th key={head} className="px-5 py-4">{head}</th>)}
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.date} className="border-t border-line">
                    <td className="px-5 py-4 font-semibold text-ink-primary">{row.date}</td>
                    <td className="px-5 py-4 text-ink-secondary">{row.checkIn}</td>
                    <td className="px-5 py-4 text-ink-secondary">{row.checkOut}</td>
                    <td className="px-5 py-4 text-ink-secondary">{row.hours}</td>
                    <td className="px-5 py-4"><Badge>{row.status}</Badge></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
        <Card className="p-5">
          <h2 className="text-lg font-extrabold text-ink-primary">Attendance Calendar</h2>
          <div className="mt-5 grid grid-cols-7 gap-2">
            {calendarDays.map((day) => (
              <div
                key={day.day}
                className={`grid aspect-square place-items-center rounded-2xl text-sm font-bold ${
                  day.status === 'present' ? 'bg-brand-successSoft text-emerald-700' : day.status === 'late' ? 'bg-brand-warningSoft text-amber-700' : 'bg-brand-blueAccent text-brand-secondary'
                }`}
              >
                {day.day}
              </div>
            ))}
          </div>
        </Card>
      </section>
      <Card className="p-5">
        <h2 className="text-lg font-extrabold text-ink-primary">Hours Trend</h2>
        <div className="mt-4 h-72">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={weeklyAttendance}>
              <CartesianGrid stroke="#E5E7EB" vertical={false} />
              <XAxis dataKey="day" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="present" fill="#3B82F6" radius={[10, 10, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Card>
    </div>
  );
}
