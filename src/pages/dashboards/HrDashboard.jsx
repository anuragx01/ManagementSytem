import React from 'react';
import { Building2, CalendarCheck, Palmtree, Users } from 'lucide-react';
import Card from '../../components/ui/Card';
import PageHeader from '../../components/ui/PageHeader';
import { LoadingIndicator } from '../../components/ui/Skeleton';
import useApiData from '../../hooks/useApiData';
import {
  attendanceApi,
  employeesApi,
  leaveApi,
  notificationsApi,
  organizationApi,
  pageContent,
  reportsApi,
} from '../../lib/api';
import { mapNotification } from '../../lib/mappers';

export default function HrDashboard() {
  const { data: company } = useApiData(() => organizationApi.company(), null, []);
  const { data: employees, loading: employeesLoading } = useApiData(
    () => employeesApi.list({ page: 0, size: 1 }),
    { content: [], totalElements: 0 },
    [],
  );
  const { data: attendanceSummary, loading: attendanceLoading } = useApiData(
    () => attendanceApi.dashboard({}),
    null,
    [],
  );
  const { data: pendingLeaves, loading: leaveLoading } = useApiData(
    () => leaveApi.pendingHr(),
    [],
    [],
  );
  const { data: departments } = useApiData(
    async () => {
      if (!company?.id) return [];
      return organizationApi.departments({ companyId: company.id });
    },
    [],
    [company?.id],
  );
  const { data: notifications } = useApiData(
    async () => pageContent(await notificationsApi.list({ size: 5 })).map(mapNotification),
    [],
    [],
  );
  const { data: employeeReport } = useApiData(
    async () => {
      if (!company?.id) return null;
      return reportsApi.employees({ companyId: company.id });
    },
    null,
    [company?.id],
  );

  return (
    <div className="page-stack">
      <PageHeader
        kicker="HR Operations"
        title="People overview"
        description="Monitor workforce attendance, leave approvals, departments, and HR notifications."
      />

      {(employeesLoading || attendanceLoading || leaveLoading) && (
        <LoadingIndicator message="Loading HR dashboard from API..." />
      )}

      <section className="card-grid sm:grid-cols-2 xl:grid-cols-4">
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Employees</p>
          <p className="mt-3 text-3xl font-extrabold text-ink-primary">{employees?.totalElements ?? employeeReport?.totalEmployees ?? '—'}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Present Today</p>
          <p className="mt-3 text-3xl font-extrabold text-ink-primary">{attendanceSummary?.present ?? '—'}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Pending Leave Approval</p>
          <p className="mt-3 text-3xl font-extrabold text-ink-primary">{pendingLeaves?.length ?? '—'}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Departments</p>
          <p className="mt-3 text-3xl font-extrabold text-ink-primary">{departments?.length ?? '—'}</p>
        </Card>
      </section>

      <section className="section-grid xl:grid-cols-2">
        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><CalendarCheck className="h-5 w-5 text-brand-primary" /> Attendance Summary</h2>
          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            <div className="rounded-2xl border border-line p-4"><p className="text-xs font-bold uppercase text-ink-secondary">Late</p><p className="mt-2 text-2xl font-extrabold">{attendanceSummary?.late ?? '—'}</p></div>
            <div className="rounded-2xl border border-line p-4"><p className="text-xs font-bold uppercase text-ink-secondary">Absent</p><p className="mt-2 text-2xl font-extrabold">{attendanceSummary?.absent ?? '—'}</p></div>
            <div className="rounded-2xl border border-line p-4"><p className="text-xs font-bold uppercase text-ink-secondary">On Leave</p><p className="mt-2 text-2xl font-extrabold">{attendanceSummary?.onLeave ?? '—'}</p></div>
            <div className="rounded-2xl border border-line p-4"><p className="text-xs font-bold uppercase text-ink-secondary">WFH</p><p className="mt-2 text-2xl font-extrabold">{attendanceSummary?.workFromHome ?? '—'}</p></div>
          </div>
        </Card>

        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><Palmtree className="h-5 w-5 text-brand-primary" /> Pending Leave Approval</h2>
          <div className="mt-4 space-y-3">
            {(pendingLeaves || []).length === 0 && <p className="text-sm text-ink-secondary">No pending leave requests.</p>}
            {(pendingLeaves || []).slice(0, 5).map((leave) => (
              <div key={leave.id} className="rounded-2xl border border-line px-4 py-3">
                <p className="text-sm font-bold text-ink-primary">{leave.employeeName || leave.employee?.name || 'Employee'}</p>
                <p className="mt-1 text-xs text-ink-secondary">{leave.fromDate} → {leave.toDate}</p>
              </div>
            ))}
          </div>
        </Card>
      </section>

      <section className="section-grid xl:grid-cols-2">
        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><Building2 className="h-5 w-5 text-brand-secondary" /> Departments</h2>
          <div className="mt-4 space-y-3">
            {(departments || []).slice(0, 6).map((department) => (
              <div key={department.id} className="rounded-2xl bg-brand-blueAccent px-4 py-3 text-sm font-semibold text-brand-secondary">
                {department.name}
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-5" interactive={false}>
          <h2 className="section-title">Notifications</h2>
          <div className="mt-4 space-y-3">
            {notifications.slice(0, 5).map((note) => (
              <div key={note.id} className="border-b border-line pb-3 last:border-0">
                <p className="text-sm font-bold text-ink-primary">{note.title}</p>
                <p className="mt-1 text-xs text-ink-secondary">{note.time}</p>
              </div>
            ))}
          </div>
        </Card>
      </section>
    </div>
  );
}
