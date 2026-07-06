import React from 'react';
import { ClipboardList, FolderKanban } from 'lucide-react';
import Card from '../../components/ui/Card';
import PageHeader from '../../components/ui/PageHeader';
import { LoadingIndicator } from '../../components/ui/Skeleton';
import useApiData from '../../hooks/useApiData';
import { organizationApi, pageContent, projectsApi, tasksApi } from '../../lib/api';
import { buildWelcomeTitle } from '../../lib/greeting';
import { mapTask } from '../../lib/mappers';

export default function TeamLeadDashboard() {
  const { data: company } = useApiData(() => organizationApi.company(), null, []);
  const { data: teamTasks, loading: tasksLoading } = useApiData(
    async () => pageContent(await tasksApi.search({ size: 20 })).map(mapTask),
    [],
    [],
  );
  const { data: projects, loading: projectsLoading } = useApiData(
    async () => {
      if (!company?.id) return { content: [] };
      return projectsApi.list({ companyId: company.id, page: 0, size: 6 });
    },
    { content: [] },
    [company?.id],
  );

  return (
    <div className="page-stack">
      <PageHeader
        kicker="Team Lead"
        title={buildWelcomeTitle('Team Lead')}
        description="Focus on team tasks, active projects, and execution status."
      />

      {(tasksLoading || projectsLoading) && <LoadingIndicator message="Loading team lead dashboard from API..." />}

      <section className="card-grid sm:grid-cols-2">
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Team Tasks</p>
          <p className="mt-3 text-3xl font-extrabold text-ink-primary">{teamTasks.length}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm font-semibold text-ink-secondary">Active Projects</p>
          <p className="mt-3 text-3xl font-extrabold text-ink-primary">{pageContent(projects).length}</p>
        </Card>
      </section>

      <section className="section-grid xl:grid-cols-2">
        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><ClipboardList className="h-5 w-5 text-brand-primary" /> Team Tasks</h2>
          <div className="mt-4 space-y-3">
            {teamTasks.slice(0, 8).map((task) => (
              <div key={task.id} className="rounded-2xl border border-line px-4 py-3">
                <p className="text-sm font-bold text-ink-primary">{task.title}</p>
                <p className="mt-1 text-xs text-ink-secondary">{task.status}</p>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-5" interactive={false}>
          <h2 className="section-title flex items-center gap-2"><FolderKanban className="h-5 w-5 text-brand-primary" /> Projects</h2>
          <div className="mt-4 space-y-3">
            {pageContent(projects).map((project) => (
              <div key={project.id} className="rounded-2xl bg-brand-blueAccent px-4 py-3">
                <p className="text-sm font-bold text-brand-secondary">{project.name}</p>
                <p className="mt-1 text-xs text-ink-secondary">{project.status}</p>
              </div>
            ))}
          </div>
        </Card>
      </section>
    </div>
  );
}

