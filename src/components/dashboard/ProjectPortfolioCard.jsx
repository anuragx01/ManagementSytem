import React from 'react';
import Badge from '../ui/Badge';
import Card from '../ui/Card';
import ProgressBar from '../ui/ProgressBar';

const defaultAvatar = 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=80&q=80';

function formatDeadline(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'short' }).format(new Date(value));
}

function statusLabel(status) {
  if (!status) return 'Planning';
  return String(status).replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
}

export default function ProjectPortfolioCard({ project, report, members = [] }) {
  const totalTasks = report?.totalTasks ?? 0;
  const doneTasks = report?.tasksByStatus?.DONE ?? 0;
  const remaining = Math.max(totalTasks - doneTasks, 0);
  const progress = totalTasks > 0 ? Math.round((doneTasks / totalTasks) * 100) : project?.progress ?? 0;
  const visibleMembers = members.slice(0, 3);
  const extraCount = Math.max(members.length - visibleMembers.length, 0);
  const priority = project?.priority || (project?.type === 'SOFTWARE' ? 'High' : 'Medium');

  return (
    <Card className="flex h-full flex-col p-5 transition duration-200 hover:-translate-y-0.5 hover:shadow-soft">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h3 className="text-lg font-extrabold text-ink-primary">{project.name}</h3>
          <p className="mt-1 text-sm font-semibold text-brand-primary">{progress}% Complete</p>
        </div>
        <Badge>{statusLabel(project.status)}</Badge>
      </div>

      <div className="mt-4">
        <ProgressBar value={progress} />
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-2">
        <div className="rounded-2xl border border-line p-3">
          <p className="text-xs font-bold uppercase text-ink-secondary">Status</p>
          <p className="mt-1 text-sm font-bold text-ink-primary">{statusLabel(project.status)}</p>
        </div>
        <div className="rounded-2xl border border-line p-3">
          <p className="text-xs font-bold uppercase text-ink-secondary">Deadline</p>
          <p className="mt-1 text-sm font-bold text-ink-primary">{formatDeadline(project.endDate)}</p>
        </div>
        <div className="rounded-2xl border border-line p-3">
          <p className="text-xs font-bold uppercase text-ink-secondary">Priority</p>
          <p className="mt-1 text-sm font-bold text-ink-primary">{priority}</p>
        </div>
        <div className="rounded-2xl border border-line p-3">
          <p className="text-xs font-bold uppercase text-ink-secondary">Tasks</p>
          <p className="mt-1 text-sm font-bold text-ink-primary">{doneTasks}/{totalTasks || '—'} done · {remaining} left</p>
        </div>
      </div>

      <div className="mt-5 rounded-2xl bg-surface-muted p-4">
        <p className="text-xs font-bold uppercase text-ink-secondary">Project Lead</p>
        <p className="mt-1 text-sm font-bold text-ink-primary">{project.ownerName || 'Unassigned'}</p>
      </div>

      <div className="mt-4 flex-1">
        <p className="text-xs font-bold uppercase text-ink-secondary">Working Members</p>
        <div className="mt-3 flex flex-wrap items-center gap-2">
          {visibleMembers.length === 0 && <span className="text-sm text-ink-secondary">No members loaded</span>}
          {visibleMembers.map((member) => (
            <div key={member.id || member.employeeId} className="flex items-center gap-2 rounded-full border border-line bg-white px-2 py-1">
              <img
                src={member.avatar || member.profilePictureUrl || defaultAvatar}
                alt={member.employeeName || member.name}
                className="h-7 w-7 rounded-full object-cover"
              />
              <span className="text-xs font-semibold text-ink-primary">{member.employeeName || member.name}</span>
            </div>
          ))}
          {extraCount > 0 && (
            <span className="rounded-full bg-brand-blueAccent px-3 py-1 text-xs font-bold text-brand-secondary">+{extraCount} More</span>
          )}
        </div>
        <p className="mt-2 text-xs text-ink-secondary">Team size: {members.length || project.memberCount || 0}</p>
      </div>
    </Card>
  );
}
