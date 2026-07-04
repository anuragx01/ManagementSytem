import React from 'react';
import { ClipboardList, Palmtree, Users } from 'lucide-react';
import Card from '../../components/ui/Card';
import PageHeader from '../../components/ui/PageHeader';
import { LoadingIndicator } from '../../components/ui/Skeleton';
import useApiData from '../../hooks/useApiData';
import { employeesApi, leaveApi, pageContent, tasksApi } from '../../lib/api';
import { buildWelcomeTitle } from '../../lib/greeting';
import { mapTask } from '../../lib/mappers';

export default function ManagerDashboard() {
  const { data: team, loading: teamLoading } = useApiData(
    () => employeesApi.list({ page: 0, size: 10 }),
    { content: [], totalElements: 0 },
    [],
  );
  const { data: pendingLeaves, loading: leaveLoading } = useApiData(
    () => leaveApi.pendingManager(),
    [],
    [],
  );
  const { data: myTasks, loading: tasksLoading } = useApiData(
    async () => pageContent(await tasksApi.search({ size: 10 })).map(mapTask),
    [],
    [],
  );
  const { data: teamTasks } = useApiData(
    async () => pageContent(await tasksApi.search({ size: 20 })).map(mapTask),
    [],
    [],
  );

  return (
    <div className="page-stack">
      <PageHeader
        kicker="Manager"
        title={buildWelcomeTitle('Manager')}
        description="Review your team, pending leave approvals, and task workload."
      />

      {(teamLoading || leaveLoading || tasksLoading) && (
        <LoadingIndicator message="Loading manager dashboard from API..." />
      )}

      <section className="card-grid sm:grid-cols-3">
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">My Team</p>
          <p className="mt-3 text-3xl font-extrabold text-ink-primary">{team?.totalElements ?? pageContent(team).length ?? '—'}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Pending Team Leave</p>
          <p className="mt-3 text-3xl font-extrabold text-ink-primary">{pendingLeaves?.length ?? '—'}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Team Tasks</p>
          <p className="mt-3 text-3xl font-extrabold text-ink-primary">{teamTasks.length}</p>
        </Card>
      </section>

      <section className="section-grid xl:grid-cols-2">
        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><Users className="h-5 w-5 text-brand-primary" /> My Team</h2>
          <div className="mt-4 space-y-3">
            {pageContent(team).slice(0, 6).map((member) => (
              <div key={member.id} className="rounded-2xl border border-line px-4 py-3">
                <p className="text-sm font-bold text-ink-primary">{member.firstName ? `${member.firstName} ${member.lastName || ''}`.trim() : member.name}</p>
                <p className="mt-1 text-xs text-ink-secondary">{member.departmentName || member.department}</p>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><Palmtree className="h-5 w-5 text-brand-primary" /> Team Leave</h2>
          <div className="mt-4 space-y-3">
            {(pendingLeaves || []).length === 0 && <p className="text-sm text-ink-secondary">No pending leave requests.</p>}
            {(pendingLeaves || []).slice(0, 5).map((leave) => (
              <div key={leave.id} className="rounded-2xl border border-line px-4 py-3">
                <p className="text-sm font-bold text-ink-primary">{leave.employeeName || 'Employee'}</p>
                <p className="mt-1 text-xs text-ink-secondary">{leave.fromDate} → {leave.toDate}</p>
              </div>
            ))}
          </div>
        </Card>
      </section>

      <Card className="p-5" interactive={false}>
        <h2 className="section-title flex items-center gap-2"><ClipboardList className="h-5 w-5 text-brand-secondary" /> My Tasks</h2>
        <div className="mt-4 space-y-3">
          {myTasks.slice(0, 5).map((task) => (
            <div key={task.id} className="rounded-2xl border border-line px-4 py-3">
              <p className="text-sm font-bold text-ink-primary">{task.title}</p>
              <p className="mt-1 text-xs text-ink-secondary">{task.status}</p>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
