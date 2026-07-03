import React, { useMemo, useState } from 'react';
import { Clock, LogIn, LogOut } from 'lucide-react';
import { Can } from '../components/RoleGate';
import { useRole } from '../context/RoleContext';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import Card from '../components/ui/Card';
import SearchFilter from '../components/ui/SearchFilter';
import { LoadingIndicator } from '../components/ui/Skeleton';
import useApiData from '../hooks/useApiData';
import { attendanceApi, pageContent } from '../lib/api';
import { formatTime, mapAttendance } from '../lib/mappers';

export default function Attendance() {
  const { activeRole } = useRole();
  const [actionStatus, setActionStatus] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const { data: today, loading: todayLoading, error: todayError, refresh: refreshToday } = useApiData(
    () => attendanceApi.today(),
    null,
    [],
  );
  const canViewAllAttendance = ['admin', 'hr'].includes(activeRole.key);
  const { data: rows, loading, error, refresh: refreshHistory } = useApiData(
    async () => {
      const attendanceSource = canViewAllAttendance ? attendanceApi.all : attendanceApi.my;
      return pageContent(await attendanceSource({ size: canViewAllAttendance ? 100 : 30 })).map(mapAttendance);
    },
    [],
    [canViewAllAttendance],
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

  const late = rawRecords.filter((record) => record.status === 'LATE').length;
  const leave = rawRecords.filter((record) => ['LEAVE', 'ON_LEAVE'].includes(record.status)).length;
  const attendancePercentage = monthlySummary.total ? Math.round((monthlySummary.present / monthlySummary.total) * 100) : 0;
  const statusOptions = useMemo(() => [...new Set(rows.map((row) => row.status).filter(Boolean))].sort(), [rows]);
  const filteredRows = useMemo(() => {
    const term = searchQuery.trim().toLowerCase();
    return rows.filter((row) => {
      const matchesSearch = !term || [row.employeeName, row.employeeCode, row.date, row.checkIn, row.checkOut, row.hours, row.status].some((value) => String(value || '').toLowerCase().includes(term));
      const matchesStatus = !statusFilter || row.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [rows, searchQuery, statusFilter]);

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

      <SearchFilter
        placeholder="Search attendance records"
        filterLabel="All Status"
        options={statusOptions}
        searchValue={searchQuery}
        onSearchChange={setSearchQuery}
        filterValue={statusFilter}
        onFilterChange={setStatusFilter}
      />

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
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Leave</p>
          <p className="mt-3 text-2xl font-extrabold text-ink-primary">{leave}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Late Arrivals</p>
          <p className="mt-3 text-2xl font-extrabold text-ink-primary">{late}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Attendance %</p>
          <p className="mt-3 text-2xl font-extrabold text-ink-primary">{attendancePercentage}%</p>
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

      <section>
        <Card className="overflow-hidden" interactive={false}>
          <div className="border-b border-line p-5">
            <h2 className="section-title">Attendance Table</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface-muted text-xs uppercase text-ink-secondary">
                <tr>
                  {(canViewAllAttendance ? ['Employee Name', 'Employee ID', 'Date', 'Check In', 'Check Out', 'Hours', 'Status'] : ['Date', 'Check In', 'Check Out', 'Hours', 'Status']).map((head) => <th key={head} className="px-5 py-4">{head}</th>)}
                </tr>
              </thead>
              <tbody>
                {filteredRows.length === 0 && (
                  <tr>
                    <td colSpan={canViewAllAttendance ? 7 : 5} className="px-5 py-8 text-center text-ink-secondary">No attendance records yet.</td>
                  </tr>
                )}
                {filteredRows.map((row) => (
                  <tr key={row.raw?.id || `${row.employeeCode}-${row.date}-${row.checkIn}`} className="border-t border-line">
                    {canViewAllAttendance && <td className="px-5 py-4 font-semibold text-ink-primary">{row.employeeName}</td>}
                    {canViewAllAttendance && <td className="px-5 py-4 text-ink-secondary">{row.employeeCode}</td>}
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
      </section>
    </div>
  );
}
