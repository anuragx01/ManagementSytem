import React, { useMemo } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, CalendarDays, Users } from 'lucide-react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import ProgressBar from '../components/ui/ProgressBar';
import useApiData from '../hooks/useApiData';
import { employeesApi, organizationApi, pageContent, projectsApi, reportsApi, tasksApi } from '../lib/api';
import { mapEmployee, mapTask } from '../lib/mappers';

function formatDate(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }).format(date);
}

function projectProgress(report, project) {
  const total = report?.totalTasks ?? 0;
  const done = report?.tasksByStatus?.DONE ?? report?.tasksByStatus?.COMPLETED ?? 0;
  if (total > 0) return Math.round((done / total) * 100);
  return project?.progress ?? 0;
}

function inferTechnology(task, member) {
  const text = `${task?.title || ''} ${task?.description || ''} ${member?.role || ''}`.toLowerCase();
  if (text.includes('react') || text.includes('frontend') || text.includes('ui')) return 'React';
  if (text.includes('node') || text.includes('api') || text.includes('backend')) return 'Node.js';
  if (text.includes('mongo') || text.includes('database') || text.includes('sql')) return 'Database';
  if (text.includes('design') || text.includes('ux')) return 'UI/UX';
  return member?.role ? String(member.role).replaceAll('_', ' ') : 'General';
}

export default function Projects() {
  const { projectId } = useParams();
  const { data: company } = useApiData(() => organizationApi.company(), null, []);
  const { data: projectsPage, loading, error } = useApiData(
    async () => (company?.id ? projectsApi.list({ companyId: company.id, page: 0, size: 50 }) : { content: [] }),
    { content: [] },
    [company?.id],
  );
  const projects = pageContent(projectsPage);
  const selectedProject = projects.find((project) => project.id === projectId);
  const { data: report } = useApiData(
    () => (projectId ? reportsApi.project(projectId) : Promise.resolve(null)),
    null,
    [projectId],
  );
  const { data: members } = useApiData(
    () => (projectId ? projectsApi.members(projectId) : Promise.resolve([])),
    [],
    [projectId],
  );
  const { data: employees } = useApiData(
    async () => pageContent(await employeesApi.list({ size: 500 })).map(mapEmployee),
    [],
    [],
  );
  const { data: tasks } = useApiData(
    async () => pageContent(await tasksApi.search(projectId ? { projectId, size: 200 } : { size: 200 })).map(mapTask),
    [],
    [projectId],
  );

  const reportByProject = useMemo(() => new Map(projects.map((project) => [project.id, null])), [projects]);

  if (projectId) {
    const progress = projectProgress(report, selectedProject || {});
    const matrix = members.map((member) => {
      const employee = employees.find((item) => item.id === member.employeeId);
      const task = tasks.find((item) => item.raw?.assigneeId === member.employeeId);
      return {
        id: member.id || member.employeeId,
        employee: member.employeeName || employee?.name || 'Team Member',
        employeeId: employee?.raw?.employeeId || '-',
        designation: employee?.role || member.role || '-',
        technology: inferTechnology(task, member),
        responsibility: task?.title || String(member.role || 'Project delivery').replaceAll('_', ' '),
      };
    });

    return (
      <div className="page-stack">
        <PageHeader
          kicker="Project Details"
          title={selectedProject?.name || 'Project'}
          description={selectedProject?.description || 'Project overview, team ownership, and delivery responsibilities.'}
          actions={<Link to="/projects" className="text-sm font-bold text-brand-primary hover:underline"><ArrowLeft className="mr-2 inline h-4 w-4" />Back to Projects</Link>}
        />
        <section className="card-grid sm:grid-cols-2 xl:grid-cols-4">
          <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">Status</p><p className="mt-2 text-2xl font-extrabold">{selectedProject?.status || '-'}</p></Card>
          <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">Deadline</p><p className="mt-2 text-2xl font-extrabold">{formatDate(selectedProject?.endDate || selectedProject?.deadline)}</p></Card>
          <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">Team Size</p><p className="mt-2 text-2xl font-extrabold">{members.length}</p></Card>
          <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">Overall Progress</p><div className="mt-4"><ProgressBar value={progress} /></div></Card>
        </section>
        <Card className="p-5" interactive={false}>
          <h2 className="section-title">Team Members</h2>
          <div className="mt-4 grid gap-3 md:grid-cols-2 xl:grid-cols-3">
            {matrix.length === 0 && <p className="text-sm text-ink-secondary">No project members loaded.</p>}
            {matrix.map((member) => (
              <div key={member.id} className="rounded-2xl border border-line p-4">
                <p className="font-bold text-ink-primary">{member.employee}</p>
                <p className="mt-1 text-sm text-ink-secondary">{member.employeeId} · {member.designation}</p>
              </div>
            ))}
          </div>
        </Card>
        <Card className="overflow-hidden" interactive={false}>
          <div className="border-b border-line p-5"><h2 className="section-title">Responsibility Matrix</h2></div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface-muted text-xs uppercase text-ink-secondary">
                <tr>{['Employee', 'Technology', 'Responsibility'].map((head) => <th key={head} className="px-5 py-4">{head}</th>)}</tr>
              </thead>
              <tbody>
                {matrix.length === 0 && <tr><td colSpan={3} className="px-5 py-8 text-center text-ink-secondary">No responsibility mapping available.</td></tr>}
                {matrix.map((member) => (
                  <tr key={member.id} className="border-t border-line">
                    <td className="px-5 py-4 font-bold text-ink-primary">{member.employee}</td>
                    <td className="px-5 py-4 text-ink-secondary">{member.technology}</td>
                    <td className="px-5 py-4 text-ink-secondary">{member.responsibility}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="page-stack">
      <PageHeader kicker="Projects" title="Project portfolio" description="All projects with status, progress, deadline, and team size." />
      {loading && <p className="loading-text">Loading projects...</p>}
      {error && <p className="alert-warning" role="alert">{error}</p>}
      <section className="card-grid md:grid-cols-2 xl:grid-cols-3">
        {projects.map((project) => {
          const progress = projectProgress(reportByProject.get(project.id), project);
          return (
            <Link key={project.id} to={`/projects/${project.id}`}>
              <Card className="h-full p-5 transition duration-200 hover:-translate-y-0.5 hover:shadow-soft">
                <div className="flex items-start justify-between gap-3">
                  <h2 className="section-title">{project.name}</h2>
                  <Badge>{project.status || 'Planning'}</Badge>
                </div>
                <p className="mt-2 line-clamp-2 text-sm text-ink-secondary">{project.description || 'No description'}</p>
                <div className="mt-5"><ProgressBar value={progress} /></div>
                <div className="mt-5 flex flex-wrap gap-3 text-sm text-ink-secondary">
                  <span className="flex items-center gap-2"><CalendarDays className="h-4 w-4" /> {formatDate(project.endDate || project.deadline)}</span>
                  <span className="flex items-center gap-2"><Users className="h-4 w-4" /> {project.memberCount || 0} members</span>
                </div>
              </Card>
            </Link>
          );
        })}
      </section>
    </div>
  );
}
