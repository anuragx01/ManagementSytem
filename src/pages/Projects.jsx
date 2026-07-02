import React from 'react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import useApiData from '../hooks/useApiData';
import { organizationApi, pageContent, projectsApi } from '../lib/api';

export default function Projects() {
  const { data: company } = useApiData(() => organizationApi.company(), null, []);
  const { data: projects, loading, error } = useApiData(
    async () => projectsApi.list({ companyId: company.id, page: 0, size: 50 }),
    { content: [] },
    [company?.id],
  );

  return (
    <div className="page-stack">
      <PageHeader kicker="Projects" title="Project portfolio" description="GET /projects?companyId=" />
      {loading && <p className="loading-text">Loading projects...</p>}
      {error && <p className="alert-warning" role="alert">{error}</p>}
      <section className="card-grid md:grid-cols-2 xl:grid-cols-3">
        {pageContent(projects).map((project) => (
          <Card key={project.id} className="p-5">
            <h2 className="section-title">{project.name}</h2>
            <p className="mt-2 text-sm text-ink-secondary">{project.description || 'No description'}</p>
            <p className="mt-4 text-xs font-bold uppercase text-brand-primary">{project.status}</p>
          </Card>
        ))}
      </section>
    </div>
  );
}
