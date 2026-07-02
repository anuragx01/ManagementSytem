import React from 'react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import { LoadingIndicator } from '../components/ui/Skeleton';
import useApiData from '../hooks/useApiData';
import { adminApi, pageContent } from '../lib/api';

export default function AuditLogs() {
  const { data, loading, error } = useApiData(
    () => adminApi.auditLogs({ page: 0, size: 50 }),
    { content: [] },
    [],
  );

  return (
    <div className="page-stack">
      <PageHeader kicker="Administration" title="Audit Logs" description="GET /admin/audit-logs" />
      {loading && <LoadingIndicator message="Loading audit logs..." />}
      {error && <p className="alert-warning" role="alert">{error}</p>}
      <Card className="overflow-hidden" interactive={false}>
        <div className="overflow-x-auto">
          <table className="w-full min-w-[960px] text-left text-sm">
            <thead className="sticky top-0 bg-surface-muted text-xs uppercase text-ink-secondary">
              <tr>
                {['Action', 'Entity', 'User', 'Timestamp'].map((head) => <th key={head} className="px-5 py-4">{head}</th>)}
              </tr>
            </thead>
            <tbody>
              {pageContent(data).map((log) => (
                <tr key={log.id} className="border-t border-line">
                  <td className="px-5 py-4 font-semibold text-ink-primary">{log.action}</td>
                  <td className="px-5 py-4 text-ink-secondary">{log.entityType}</td>
                  <td className="px-5 py-4 text-ink-secondary">{log.userId || log.performedBy}</td>
                  <td className="px-5 py-4 text-ink-secondary">{log.createdAt || log.timestamp}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
