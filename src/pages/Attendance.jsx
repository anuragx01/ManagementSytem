import React, { useMemo, useState } from 'react';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Clock, LogIn, LogOut } from 'lucide-react';
import { Can } from '../components/RoleGate';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import Card from '../components/ui/Card';
import { LoadingIndicator } from '../components/ui/Skeleton';
import useApiData from '../hooks/useApiData';
import { attendanceApi, pageContent } from '../lib/api';
import { formatTime, mapAttendance } from '../lib/mappers';

const weekdayLabels = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export default function Attendance() {
  const [actionStatus, setActionStatus] = useState('');
  const { data: today, loading: todayLoading, error: todayError, refresh: refreshToday } = useApiData(
    () => attendanceApi.today(),
    null,
    [],
  );
  const { data: rows, loading, error, refresh: refreshHistory } = useApiData(
    async () => pageContent(await attendanceApi.my({ size: 30 })).map(mapAttendance),
    [],
    [],
  );

  const summary = [
    { label: 'Check In', value: formatTime(today?.clockIn), icon: LogIn },
    { label: 'Check Out', value: formatTime(today?.clockOut), icon: LogOut },
    { label: "Today's Working Hours", value: today?.workedHours || '—', icon: Clock },
  ];

  const rawRecords = useMemo(() => rows.map((row) => row.raw || row), [rows]);

  const monthlySummary = useMemo(() => {
    const now = new Date();
    const prefix = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    const monthRecords = rawRecords.filter((record) => record.date?.startsWith(prefix));
    const present = monthRecords.filter((record) => ['PRESENT', 'LATE', 'WORK_FROM_HOME'].includes(record.status)).length;
    const absent = monthRecords.filter((record) => record.status === 'ABSENT').length;
    return { present, absent, total: monthRecords.length };
  }, [rawRecords]);

  const weeklyAttendance = useMemo(() => {
    return weekdayLabels.map((day) => ({
      day,
      present: rawRecords.filter((record) => {
        if (!record.date) return false;
        const date = new Date(record.date);
        return weekdayLabels[date.getDay()] === day && record.status === 'PRESENT';
      }).length,
    }));
  }, [rawRecords]);

  const calendarDays = useMemo(() => {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const recordsByDay = new Map(
      rawRecords
        .filter((record) => record.date?.startsWith(`${year}-${String(month + 1).padStart(2, '0')}`))
        .map((record) => [Number(record.date.split('-')[2]), record.status?.toLowerCase()]),
    );

    return Array.from({ length: daysInMonth }, (_, index) => {
      const day = index + 1;
      const status = recordsByDay.get(day);
      return {
        day,
        status: status === 'present' ? 'present' : status === 'late' ? 'late' : 'absent',
      };
    });
  }, [rawRecords]);

  async function handleClockIn() {
    setActionStatus('Clocking in...');
    try {
      await attendanceApi.clockIn({});
      refreshToday();
      refreshHistory();
      setActionStatus('Clocked in successfully.');
    } catch (err) {
      setActionStatus(err.message);
    }
  }

  async function handleClockOut() {
    setActionStatus('Clocking out...');
    try {
      await attendanceApi.clockOut({});
      refreshToday();
      refreshHistory();
      setActionStatus('Clocked out successfully.');
    } catch (err) {
      setActionStatus(err.message);
    }
  }

  return (
    <div className="page-stack">
      <div className="page-header">
        <p className="page-kicker">Attendance</p>
        <h1 className="page-title">Daily time overview</h1>
      </div>

      <div className="flex flex-wrap gap-3">
        <Button onClick={handleClockIn} disabled={Boolean(today?.clockIn && !today?.clockOut)}>
          <LogIn className="h-4 w-4" /> Clock In
        </Button>
        <Button variant="secondary" onClick={handleClockOut} disabled={!today?.clockIn || Boolean(today?.clockOut)}>
          <LogOut className="h-4 w-4" /> Clock Out
        </Button>
      </div>

      {actionStatus && <p className="alert-info" role="status">{actionStatus}</p>}
      {(todayError || error) && <p className="alert-warning" role="alert">{todayError || error}</p>}
      {(todayLoading || loading) && <LoadingIndicator message="Loading attendance from backend..." />}

      <section className="card-grid sm:grid-cols-2 xl:grid-cols-4">
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Attendance Status</p>
          <p className="mt-3 text-2xl font-extrabold capitalize text-ink-primary">{today?.status?.toLowerCase?.() || today?.status || 'Not checked in'}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Daily Hours</p>
          <p className="mt-3 text-2xl font-extrabold text-ink-primary">{today?.workedHours || '—'}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Monthly Present</p>
          <p className="mt-3 text-2xl font-extrabold text-ink-primary">{monthlySummary.present}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Monthly Absent</p>
          <p className="mt-3 text-2xl font-extrabold text-ink-primary">{monthlySummary.absent}</p>
        </Card>
      </section>

      <section className="card-grid md:grid-cols-3">
        {summary.map((item) => {
          const Icon = item.icon;
          return (
            <Card key={item.label} className="p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-semibold text-ink-secondary">{item.label}</p>
                  <p className="mt-3 text-3xl font-extrabold text-ink-primary">{item.value}</p>
                </div>
                <div className="stat-icon-wrap bg-brand-blueAccent text-brand-secondary"><Icon className="h-5 w-5" /></div>
              </div>
            </Card>
          );
        })}
      </section>

      <Can roles={['admin', 'hr']}>
        <Card className="flex flex-col justify-between gap-4 p-5 sm:flex-row sm:items-center" interactive={false}>
          <div>
            <h2 className="section-title">Attendance administration</h2>
            <p className="mt-1 text-sm text-ink-secondary">HR and Admin can view attendance records and update exceptions via POST /attendance/regularize.</p>
          </div>
        </Card>
      </Can>

      <section className="section-grid xl:grid-cols-[1fr_0.8fr]">
        <Card className="overflow-hidden" interactive={false}>
          <div className="border-b border-line p-5">
            <h2 className="section-title">Attendance Table</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface-muted text-xs uppercase text-ink-secondary">
                <tr>
                  {['Date', 'Check In', 'Check Out', 'Hours', 'Status'].map((head) => <th key={head} className="px-5 py-4">{head}</th>)}
                </tr>
              </thead>
              <tbody>
                {rows.length === 0 && (
                  <tr>
                    <td colSpan={5} className="px-5 py-8 text-center text-ink-secondary">No attendance records yet.</td>
                  </tr>
                )}
                {rows.map((row) => (
                  <tr key={row.date + row.checkIn} className="border-t border-line">
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
        <Card className="p-5" interactive={false}>
          <h2 className="section-title">Attendance Calendar</h2>
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
      <Card className="p-5" interactive={false}>
        <h2 className="section-title">Hours Trend</h2>
        <div className="mt-4 h-72">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={weeklyAttendance}>
              <CartesianGrid stroke="#E2E6ED" vertical={false} />
              <XAxis dataKey="day" stroke="#667085" tick={{ fontSize: 12 }} />
              <YAxis stroke="#667085" tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="present" fill="#1B2A4A" radius={[10, 10, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Card>
    </div>
  );
}
