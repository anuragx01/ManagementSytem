import React from 'react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import useApiData from '../hooks/useApiData';
import { organizationApi } from '../lib/api';

export default function Settings() {
  const { data: company, loading, error } = useApiData(() => organizationApi.company(), null, []);

  return (
    <div className="page-stack">
      <PageHeader kicker="Settings" title="Organization settings" description="GET /organization/company · PUT /organization/company (SUPER_ADMIN, HR)" />
      {loading && <p className="loading-text">Loading settings...</p>}
      {error && <p className="alert-warning" role="alert">{error}</p>}
      <Card className="p-5" interactive={false}>
        <p className="text-sm text-ink-secondary">Company configuration is read from the backend. Editing requires PUT /organization/company with admin or HR permissions.</p>
        <div className="mt-5 rounded-2xl border border-line p-4">
          <p className="font-bold text-ink-primary">{company?.name || 'Company'}</p>
          <p className="mt-1 text-sm text-ink-secondary">{company?.email}</p>
        </div>
      </Card>
    </div>
  );
}
