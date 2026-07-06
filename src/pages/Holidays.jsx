import React from 'react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import useApiData from '../hooks/useApiData';
import { employeesApi, organizationApi } from '../lib/api';

export default function Holidays() {
  const { data: profile } = useApiData(() => employeesApi.me(), null, []);
  const { data: holidays, loading, error } = useApiData(
    async () => organizationApi.holidays({ companyId: profile?.companyId || profile?.raw?.companyId }),
    [],
    [profile?.companyId, profile?.raw?.companyId],
  );

  return (
    <div className="page-stack">
      <PageHeader kicker="Holidays" title="Company holiday calendar" description="GET /organization/holidays?companyId=" />
      {loading && <p className="loading-text">Loading holidays...</p>}
      {error && <p className="alert-warning" role="alert">{error}</p>}
      <Card className="p-5" interactive={false}>
        <div className="space-y-3">
          {(holidays || []).map((holiday) => (
            <div key={holiday.id || holiday.name} className="flex items-center justify-between rounded-2xl border border-line px-4 py-3">
              <span className="font-bold text-ink-primary">{holiday.name}</span>
              <span className="text-sm text-ink-secondary">{holiday.date}</span>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}

