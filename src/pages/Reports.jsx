import React, { useState } from 'react';
import { useRole } from '../context/RoleContext';
import PageHeader from '../components/ui/PageHeader';
import PageTabs from '../components/ui/PageTabs';
import Card from '../components/ui/Card';
import useApiData from '../hooks/useApiData';
import { organizationApi, reportsApi } from '../lib/api';

export default function Reports() {
  const { activeRole } = useRole();
  const tabs = [
    { id: 'attendance', label: 'Attendance Reports' },
    { id: 'leave', label: 'Leave Reports' },
    { id: 'employees', label: 'Employee Reports' },
  ];
  const [activeTab, setActiveTab] = useState('attendance');
  const { data: company } = useApiData(() => organizationApi.company(), null, []);
  const from = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().slice(0, 10);
  const to = new Date().toISOString().slice(0, 10);

  const { data: attendanceReport, loading, error } = useApiData(
    async () => {
      if (activeTab !== 'attendance') return null;
      return reportsApi.attendance({ companyId: company?.id, from, to });
    },
    null,
    [activeTab, company?.id],
  );
  const { data: leaveReport } = useApiData(
    async () => activeTab === 'leave' ? reportsApi.leave({ companyId: company?.id, from, to }) : null,
    null,
    [activeTab, company?.id],
  );
  const { data: employeeReport } = useApiData(
    async () => activeTab === 'employees' ? reportsApi.employees({ companyId: company?.id }) : null,
    null,
    [activeTab, company?.id],
  );

  return (
    <div className="page-stack">
      <PageHeader
        kicker="Reports"
        title={`${activeRole.label} reports`}
        description="GET /reports/attendance | /reports/leave | /reports/employees"
      />
      <Card className="overflow-hidden" interactive={false}>
        <div className="px-5 pt-5"><PageTabs tabs={tabs} activeTab={activeTab} onChange={setActiveTab} /></div>
        <div className="p-5">
          {loading && <p className="loading-text">Loading report...</p>}
          {error && <p className="alert-warning" role="alert">{error}</p>}
          {activeTab === 'attendance' && attendanceReport && (
            <div className="grid gap-4 sm:grid-cols-3">
              <div className="rounded-2xl border border-line p-4"><p className="text-xs uppercase text-ink-secondary">Present</p><p className="mt-2 text-2xl font-extrabold">{attendanceReport.presentCount}</p></div>
              <div className="rounded-2xl border border-line p-4"><p className="text-xs uppercase text-ink-secondary">Absent</p><p className="mt-2 text-2xl font-extrabold">{attendanceReport.absentCount}</p></div>
              <div className="rounded-2xl border border-line p-4"><p className="text-xs uppercase text-ink-secondary">Late</p><p className="mt-2 text-2xl font-extrabold">{attendanceReport.lateCount}</p></div>
            </div>
          )}
          {activeTab === 'leave' && leaveReport && (
            <div className="space-y-3">
              {(leaveReport.byType || []).map((item) => (
                <div key={item.leaveType} className="rounded-2xl border border-line px-4 py-3 flex justify-between">
                  <span className="font-bold text-ink-primary">{item.leaveType}</span>
                  <span className="text-sm text-ink-secondary">Approved {item.approved} · Pending {item.pending}</span>
                </div>
              ))}
            </div>
          )}
          {activeTab === 'employees' && employeeReport && (
            <div className="rounded-2xl border border-line p-4">
              <p className="text-xs uppercase text-ink-secondary">Total Employees</p>
              <p className="mt-2 text-2xl font-extrabold">{employeeReport.total}</p>
            </div>
          )}
        </div>
      </Card>
    </div>
  );
}
