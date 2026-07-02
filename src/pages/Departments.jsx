import React from 'react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import useApiData from '../hooks/useApiData';
import { organizationApi } from '../lib/api';

export default function Departments() {
  const { data: company } = useApiData(() => organizationApi.company(), null, []);
  const { data: departments, loading, error } = useApiData(
    async () => organizationApi.departments({ companyId: company.id }),
    [],
    [company?.id],
  );

  return (
    <div className="page-stack">
      <PageHeader kicker="Organization" title="Departments" description="GET /organization/departments?companyId=" />
      {loading && <p className="loading-text">Loading departments...</p>}
      {error && <p className="alert-warning" role="alert">{error}</p>}
      <Card className="overflow-hidden" interactive={false}>
        <table className="w-full text-left text-sm">
          <thead className="bg-surface-muted text-xs uppercase text-ink-secondary">
            <tr><th className="px-5 py-4">Name</th><th className="px-5 py-4">Code</th><th className="px-5 py-4">Status</th></tr>
          </thead>
          <tbody>
            {(departments || []).map((department) => (
              <tr key={department.id} className="border-t border-line">
                <td className="px-5 py-4 font-semibold text-ink-primary">{department.name}</td>
                <td className="px-5 py-4 text-ink-secondary">{department.code}</td>
                <td className="px-5 py-4 text-ink-secondary">{department.active === false ? 'Inactive' : 'Active'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
