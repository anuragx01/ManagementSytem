import React, { useMemo, useState } from 'react';
import { Check, MessageSquare, Play, Plus, RefreshCw, TrendingUp, UserCheck } from 'lucide-react';
import { Can } from '../components/RoleGate';
import { useRole } from '../context/RoleContext';
import AssignTaskModal from '../components/tasks/AssignTaskModal';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import ProgressBar from '../components/ui/ProgressBar';
import { LoadingIndicator } from '../components/ui/Skeleton';
import useApiData from '../hooks/useApiData';
import { employeesApi, organizationApi, pageContent, tasksApi } from '../lib/api';
import { mapTask } from '../lib/mappers';

export default function Tasks() {
  const { activeRole } = useRole();
  const [assignOpen, setAssignOpen] = useState(false);
  const [taskStatus, setTaskStatus] = useState('');
  const [expandedId, setExpandedId] = useState(null);
  const [commentDraft, setCommentDraft] = useState('');
  const [attachments, setAttachments] = useState([]);
  const [detailCache, setDetailCache] = useState({});

  const { data: profile } = useApiData(() => employeesApi.me(), null, []);
  const { data: company } = useApiData(() => organizationApi.company(), null, []);
  const { data: taskItems, loading, error, refresh } = useApiData(
    async () => {
      const params = activeRole.key === 'employee' && profile?.id
        ? { assigneeId: profile.id, size: 50 }
        : { size: 50 };
      return pageContent(await tasksApi.search(params)).map(mapTask);
    },
    [],
    [profile?.id, activeRole.key],
  );

  const grouped = useMemo(() => ({
    pending: taskItems.filter((task) => ['Pending', 'Backlog', 'Todo'].includes(task.status)),
    inProgress: taskItems.filter((task) => ['In Progress', 'In Review', 'Testing', 'Blocked'].includes(task.status)),
    completed: taskItems.filter((task) => ['Done', 'Completed'].includes(task.status)),
  }), [taskItems]);

  async function updateTaskStatus(task, status) {
    setTaskStatus(`Updating ${task.title}...`);
    try {
      await tasksApi.update(task.id, { status });
      refresh();
      setTaskStatus('Task updated successfully.');
    } catch (err) {
      setTaskStatus(err.message);
    }
  }

  async function loadTaskDetails(taskId) {
    if (detailCache[taskId]) return;
    try {
      const [comments, activity] = await Promise.all([
        tasksApi.comments(taskId),
        tasksApi.activity(taskId, { page: 0 }),
      ]);
      setDetailCache((current) => ({
        ...current,
        [taskId]: {
          comments: Array.isArray(comments) ? comments : [],
          activity: pageContent(activity),
        },
      }));
    } catch {
      setDetailCache((current) => ({ ...current, [taskId]: { comments: [], activity: [] } }));
    }
  }

  async function toggleExpanded(task) {
    const next = expandedId === task.id ? null : task.id;
    setExpandedId(next);
    if (next) await loadTaskDetails(task.id);
  }

  async function submitComment(taskId) {
    if (!commentDraft.trim()) return;
    setTaskStatus('Adding comment...');
    try {
      await tasksApi.addComment(taskId, commentDraft.trim());
      setCommentDraft('');
      setDetailCache((current) => ({ ...current, [taskId]: undefined }));
      await loadTaskDetails(taskId);
      setTaskStatus('Comment added.');
    } catch (err) {
      setTaskStatus(err.message);
    }
  }

  return (
    <div className="page-stack">
      <div className="page-header">
        <p className="page-kicker">Task Management</p>
        <h1 className="page-title">{activeRole.key === 'employee' ? 'My assigned work' : 'Assigned work'}</h1>
      </div>
      {error && <p className="alert-warning" role="alert">{error}</p>}
      {loading && <LoadingIndicator message="Loading tasks from backend..." />}
      {taskStatus && <p className="alert-info" role="status">{taskStatus}</p>}

      <Can roles={['admin', 'manager']}>
        <Card className="flex flex-col justify-between gap-4 p-5 sm:flex-row sm:items-center" interactive={false}>
          <div>
            <h2 className="section-title">Task assignment controls</h2>
            <p className="mt-1 text-sm text-ink-secondary">Assign work to individuals, teams, or departments using the assignment modal.</p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button onClick={() => setAssignOpen(true)}><Plus className="h-4 w-4" /> Assign Task</Button>
            <Button variant="secondary"><UserCheck className="h-4 w-4" /> Review Completed</Button>
            <Button variant="blue"><TrendingUp className="h-4 w-4" /> Team Performance</Button>
          </div>
        </Card>
      </Can>

      <AssignTaskModal
        open={assignOpen}
        onClose={() => setAssignOpen(false)}
        companyId={company?.id}
        onAssigned={(count) => {
          refresh();
          setTaskStatus(`${count} task assignment(s) created.`);
        }}
      />

      <section className="card-grid sm:grid-cols-3">
        <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">Pending</p><p className="mt-2 text-3xl font-extrabold">{grouped.pending.length}</p></Card>
        <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">In Progress</p><p className="mt-2 text-3xl font-extrabold">{grouped.inProgress.length}</p></Card>
        <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">Completed</p><p className="mt-2 text-3xl font-extrabold">{grouped.completed.length}</p></Card>
      </section>

      <section className="card-grid lg:grid-cols-2">
        {taskItems.map((task) => {
          const details = detailCache[task.id];
          const isExpanded = expandedId === task.id;
          return (
            <Card key={task.id} className="p-5">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h2 className="section-title">{task.title}</h2>
                  <p className="mt-2 text-sm leading-6 text-ink-secondary">{task.description}</p>
                </div>
                <Badge>{task.priority}</Badge>
              </div>
              <div className="mt-5 flex flex-wrap items-center gap-3 text-sm">
                <span className="font-semibold text-ink-secondary">Due {task.dueDate}</span>
                <Badge>{task.status}</Badge>
              </div>
              <div className="mt-5">
                <div className="mb-2 flex justify-between text-sm font-semibold">
                  <span className="text-ink-secondary">Progress</span>
                  <span className="text-brand-primary">{task.progress}%</span>
                </div>
                <ProgressBar value={task.progress} />
              </div>
              <div className="mt-6 flex flex-wrap gap-3">
                <Button onClick={() => updateTaskStatus(task, 'IN_PROGRESS')}><Play className="h-4 w-4" /> Start Task</Button>
                <Button variant="secondary" onClick={() => updateTaskStatus(task, 'IN_REVIEW')}><RefreshCw className="h-4 w-4" /> Update Progress</Button>
                <Button variant="secondary" onClick={() => updateTaskStatus(task, 'DONE')}><Check className="h-4 w-4" /> Mark Completed</Button>
                <Button variant="secondary" onClick={() => toggleExpanded(task)}><MessageSquare className="h-4 w-4" /> {isExpanded ? 'Hide Details' : 'Details'}</Button>
              </div>

              {isExpanded && (
                <div className="mt-6 space-y-4 border-t border-line pt-5">
                  <div>
                    <p className="text-sm font-bold text-ink-primary">Comments</p>
                    <div className="mt-3 space-y-2">
                      {(details?.comments || []).length === 0 && <p className="text-xs text-ink-secondary">No comments yet.</p>}
                      {(details?.comments || []).map((comment) => (
                        <div key={comment.id} className="rounded-xl bg-surface-muted px-3 py-2 text-sm">
                          <p className="font-semibold text-ink-primary">{comment.authorName || 'User'}</p>
                          <p className="text-ink-secondary">{comment.content}</p>
                        </div>
                      ))}
                    </div>
                    <div className="mt-3 flex gap-2">
                      <input className="field-control" value={commentDraft} onChange={(event) => setCommentDraft(event.target.value)} placeholder="Add a comment..." />
                      <Button onClick={() => submitComment(task.id)}>Add</Button>
                    </div>
                  </div>

                  <div>
                    <p className="text-sm font-bold text-ink-primary">Activity History</p>
                    <div className="mt-3 space-y-2">
                      {(details?.activity || []).length === 0 && <p className="text-xs text-ink-secondary">No activity recorded.</p>}
                      {(details?.activity || []).slice(0, 5).map((entry) => (
                        <div key={entry.id} className="rounded-xl border border-line px-3 py-2 text-xs">
                          <p className="font-semibold text-ink-primary">{entry.detail || entry.action}</p>
                          <p className="text-ink-secondary">{entry.createdAt}</p>
                        </div>
                      ))}
                    </div>
                  </div>

                  <label className="block">
                    <span className="text-sm font-semibold text-ink-primary">Attachments</span>
                    <input type="file" multiple className="field-control mt-2" onChange={(event) => setAttachments(Array.from(event.target.files || []))} />
                    {attachments.length > 0 && <p className="mt-1 text-xs text-ink-secondary">{attachments.length} file(s) selected — task attachment API pending backend support.</p>}
                  </label>
                </div>
              )}
            </Card>
          );
        })}
      </section>
    </div>
  );
}
