import React from 'react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import useApiData from '../hooks/useApiData';
import { organizationApi } from '../lib/api';

export default function Organization() {
  const { data: company, loading, error } = useApiData(() => organizationApi.company(), null, []);

  return (
    <div className="page-stack">
      <PageHeader kicker="Organization" title="Company profile" description="GET /organization/company" />
      {loading && <p className="loading-text">Loading company profile...</p>}
      {error && <p className="alert-warning" role="alert">{error}</p>}
      <Card className="p-5" interactive={false}>
        <div className="grid gap-4 sm:grid-cols-2">
          {[['Name', company?.name], ['Legal Name', company?.legalName], ['Email', company?.email], ['Phone', company?.phone], ['Website', company?.website]].map(([label, value]) => (
            <div key={label} className="rounded-2xl border border-line p-4">
              <p className="text-xs font-bold uppercase text-ink-secondary">{label}</p>
              <p className="mt-2 font-bold text-ink-primary">{value || '—'}</p>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
