import React from "react";
import { useState } from 'react';
import { Check, Play, Plus, RefreshCw, TrendingUp, UserCheck } from 'lucide-react';
import { Can } from '../components/RoleGate';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import ProgressBar from '../components/ui/ProgressBar';
import { tasks } from '../data/mockData';
import useApiData from '../hooks/useApiData';
import { pageContent, tasksApi } from '../lib/api';
import { mapTask } from '../lib/mappers';

export default function Tasks() {
  const [showAssignForm, setShowAssignForm] = useState(false);
  const [taskForm, setTaskForm] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
    status: 'BACKLOG',
    dueDate: '',
  });
  const [taskStatus, setTaskStatus] = useState('');
  const { data: taskItems, loading, error } = useApiData(
    async () => pageContent(await tasksApi.search({ size: 50 })).map(mapTask),
    tasks,
    [],
  );

  async function submitTask(event) {
    event.preventDefault();
    setTaskStatus('Assigning task...');
    try {
      await tasksApi.create(taskForm);
      setTaskStatus('Task assigned successfully.');
      setShowAssignForm(false);
    } catch (err) {
      setTaskStatus(`Task assign failed: ${err.message}`);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-bold text-brand-primary">Task Management</p>
        <h1 className="mt-1 text-3xl font-extrabold text-ink-primary">Assigned work</h1>
      </div>
      {error && <p className="rounded-2xl bg-orange-50 px-4 py-3 text-sm font-semibold text-orange-700">Showing mock tasks because API is unavailable: {error}</p>}
      {loading && <p className="text-sm font-semibold text-ink-secondary">Loading tasks from backend...</p>}
      <Can roles={['admin', 'manager']}>
        <Card className="flex flex-col justify-between gap-4 p-5 sm:flex-row sm:items-center">
          <div>
            <h2 className="text-lg font-extrabold text-ink-primary">Task assignment controls</h2>
            <p className="mt-1 text-sm text-ink-secondary">Admins and managers can assign work and review completed tasks.</p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button onClick={() => setShowAssignForm((value) => !value)}><Plus className="h-4 w-4" /> Assign Task</Button>
            <Button variant="secondary"><UserCheck className="h-4 w-4" /> Review Completed</Button>
            <Button variant="blue"><TrendingUp className="h-4 w-4" /> Team Performance</Button>
          </div>
        </Card>
      </Can>
      {taskStatus && <p className="rounded-2xl bg-brand-blueAccent px-4 py-3 text-sm font-semibold text-brand-secondary">{taskStatus}</p>}
      {showAssignForm && (
        <Can roles={['admin', 'manager']}>
          <Card className="p-5">
            <h2 className="text-lg font-extrabold text-ink-primary">Assign Task</h2>
            <form className="mt-5 grid gap-4 md:grid-cols-2" onSubmit={submitTask}>
              <label className="block md:col-span-2">
                <span className="text-sm font-semibold text-ink-primary">Title</span>
                <input className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none focus:border-brand-primary focus:ring-4 focus:ring-red-100" value={taskForm.title} onChange={(event) => setTaskForm((current) => ({ ...current, title: event.target.value }))} required />
              </label>
              <label className="block md:col-span-2">
                <span className="text-sm font-semibold text-ink-primary">Description</span>
                <textarea className="mt-2 min-h-24 w-full rounded-2xl border border-line px-4 py-3 outline-none focus:border-brand-primary focus:ring-4 focus:ring-red-100" value={taskForm.description} onChange={(event) => setTaskForm((current) => ({ ...current, description: event.target.value }))} />
              </label>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Priority</span>
                <select className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none" value={taskForm.priority} onChange={(event) => setTaskForm((current) => ({ ...current, priority: event.target.value }))}>
                  <option>HIGH</option>
                  <option>MEDIUM</option>
                  <option>LOW</option>
                </select>
              </label>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Due Date</span>
                <input type="date" className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none" value={taskForm.dueDate} onChange={(event) => setTaskForm((current) => ({ ...current, dueDate: event.target.value }))} />
              </label>
              <div className="md:col-span-2">
                <Button type="submit">Assign Task</Button>
              </div>
            </form>
          </Card>
        </Can>
      )}
      <section className="grid gap-5 lg:grid-cols-2">
        {taskItems.map((task) => (
          <Card key={task.id} className="p-5">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 className="text-xl font-extrabold text-ink-primary">{task.title}</h2>
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
              <Can roles={['employee', 'admin', 'manager']}>
                <Button onClick={() => tasksApi.update(task.id, { status: 'IN_PROGRESS' })}><Play className="h-4 w-4" /> Start</Button>
                <Button variant="secondary" onClick={() => tasksApi.update(task.id, { status: 'IN_REVIEW' })}><RefreshCw className="h-4 w-4" /> Update</Button>
                <Button variant="secondary" onClick={() => tasksApi.update(task.id, { status: 'DONE' })}><Check className="h-4 w-4" /> Complete</Button>
              </Can>
            </div>
          </Card>
        ))}
      </section>
    </div>
  );
}
