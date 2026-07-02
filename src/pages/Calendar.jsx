import React from 'react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import useApiData from '../hooks/useApiData';
import { employeesApi, leaveApi, organizationApi } from '../lib/api';

export default function Calendar() {
  const { data: profile } = useApiData(() => employeesApi.me(), null, []);
  const from = new Date().toISOString().slice(0, 10);
  const to = new Date(Date.now() + 1000 * 60 * 60 * 24 * 30).toISOString().slice(0, 10);
  const { data: leaves } = useApiData(() => leaveApi.calendar({ from, to }), [], []);
  const { data: holidays } = useApiData(
    async () => organizationApi.holidays({ companyId: profile?.companyId || profile?.raw?.companyId }),
    [],
    [profile?.companyId, profile?.raw?.companyId],
  );

  return (
    <div className="page-stack">
      <PageHeader kicker="Calendar" title="Leave & holiday calendar" description="GET /leaves/calendar and GET /organization/holidays" />
      <section className="section-grid xl:grid-cols-2">
        <Card className="p-5" interactive={false}>
          <h2 className="section-title">Approved Leaves</h2>
          <div className="mt-4 space-y-3">
            {(leaves || []).map((leave) => (
              <div key={leave.id} className="rounded-2xl border border-line px-4 py-3">
                <p className="text-sm font-bold text-ink-primary">{leave.employeeName || 'Employee'}</p>
                <p className="mt-1 text-xs text-ink-secondary">{leave.startDate || leave.fromDate} → {leave.endDate || leave.toDate}</p>
              </div>
            ))}
          </div>
        </Card>
        <Card className="p-5" interactive={false}>
          <h2 className="section-title">Holidays</h2>
          <div className="mt-4 space-y-3">
            {(holidays || []).map((holiday) => (
              <div key={holiday.id || holiday.name} className="rounded-2xl bg-brand-blueAccent px-4 py-3">
                <p className="text-sm font-bold text-brand-secondary">{holiday.name}</p>
                <p className="mt-1 text-xs text-ink-secondary">{holiday.date}</p>
              </div>
            ))}
          </div>
        </Card>
      </section>
    </div>
  );
}
