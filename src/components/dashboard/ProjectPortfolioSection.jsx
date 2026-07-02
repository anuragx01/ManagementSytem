import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Card from '../ui/Card';
import { LoadingIndicator } from '../ui/Skeleton';
import ProjectPortfolioCard from './ProjectPortfolioCard';
import { pageContent, projectsApi, reportsApi } from '../../lib/api';

export default function ProjectPortfolioSection({ companyId }) {
  const [projects, setProjects] = useState([]);
  const [meta, setMeta] = useState({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!companyId) return;
    let active = true;
    async function load() {
      setLoading(true);
      try {
        const list = pageContent(await projectsApi.list({ companyId, page: 0, size: 12 }));
        if (!active) return;
        setProjects(list);
        const details = await Promise.all(
          list.map(async (project) => {
            try {
              const [report, members] = await Promise.all([
                reportsApi.project(project.id),
                projectsApi.members(project.id),
              ]);
              return [project.id, { report, members: Array.isArray(members) ? members : [] }];
            } catch {
              return [project.id, { report: null, members: [] }];
            }
          }),
        );
        if (active) setMeta(Object.fromEntries(details));
      } finally {
        if (active) setLoading(false);
      }
    }
    load();
    return () => { active = false; };
  }, [companyId]);

  return (
    <section className="space-y-4">
      <div className="flex items-end justify-between gap-4">
        <div>
          <h2 className="section-title">Project Portfolio</h2>
          <p className="mt-1 text-sm text-ink-secondary">Live project progress, team allocation, and delivery status.</p>
        </div>
        <Link to="/projects" className="text-sm font-bold text-brand-primary hover:underline">View all projects</Link>
      </div>
      {loading && <LoadingIndicator message="Loading project portfolio..." />}
      {!loading && projects.length === 0 && (
        <Card className="p-5" interactive={false}>
          <p className="text-sm text-ink-secondary">No projects available yet.</p>
        </Card>
      )}
      <div className="card-grid md:grid-cols-2 xl:grid-cols-3">
        {projects.map((project) => (
          <ProjectPortfolioCard
            key={project.id}
            project={project}
            report={meta[project.id]?.report}
            members={meta[project.id]?.members || []}
          />
        ))}
      </div>
    </section>
  );
}
