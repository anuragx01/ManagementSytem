import React, { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Building2,
  CalendarClock,
  CheckCircle2,
  ClipboardCheck,
  ClipboardList,
  Edit3,
  FileBarChart,
  FolderKanban,
  Megaphone,
  Plus,
  TrendingUp,
  Trash2,
  UserPlus,
  Users,
} from 'lucide-react';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Modal, { ModalActions } from '../../components/ui/Modal';
import PageHeader from '../../components/ui/PageHeader';
import ProgressBar from '../../components/ui/ProgressBar';
import { LoadingIndicator } from '../../components/ui/Skeleton';
import useApiData from '../../hooks/useApiData';
import { buildWelcomeTitle } from '../../lib/greeting';
import {
  attendanceApi,
  employeesApi,
  leaveApi,
  notificationsApi,
  organizationApi,
  pageContent,
  projectsApi,
  reportsApi,
  tasksApi,
} from '../../lib/api';
import { mapNotification } from '../../lib/mappers';

const ANNOUNCEMENTS_KEY = 'nexstar-admin-announcements';

function formatNumber(value) {
  if (value === undefined || value === null || Number.isNaN(Number(value))) return '-';
  return new Intl.NumberFormat('en-IN').format(value);
}

function formatDate(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: 'short' }).format(date);
}

function titleCase(value = '') {
  return String(value).replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function taskStatus(task) {
  return task.status || task.raw?.status || 'TODO';
}

function projectStatus(project) {
  const status = titleCase(project.status || 'PLANNING');
  if (/progress|active/i.test(status)) return 'Active';
  if (/risk|blocked|hold/i.test(status)) return 'At Risk';
  if (/done|complete/i.test(status)) return 'Completed';
  return status || 'Planning';
}

function projectProgress(report, project) {
  const total = report?.totalTasks ?? 0;
  const done = report?.tasksByStatus?.DONE ?? report?.tasksByStatus?.COMPLETED ?? 0;
  if (total > 0) return Math.round((done / total) * 100);
  return project.progress ?? 0;
}

function isCurrentMonth(value) {
  if (!value) return false;
  const date = new Date(value);
  const now = new Date();
  return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth();
}

function KpiCard({ icon: Icon, title, value, trend, accent = 'bg-brand-blueAccent text-brand-secondary' }) {
  return (
    <Card className="p-5 transition duration-200 hover:-translate-y-0.5 hover:shadow-soft" interactive={false}>
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-ink-secondary">{title}</p>
          <p className="mt-3 text-3xl font-extrabold text-ink-primary">{value}</p>
          {trend && <p className="mt-2 text-xs font-bold text-emerald-700">{trend}</p>}
        </div>
        <div className={`grid h-12 w-12 shrink-0 place-items-center rounded-2xl ${accent}`}>
          <Icon className="h-5 w-5" />
        </div>
      </div>
    </Card>
  );
}

function EmptyState({ message }) {
  return (
    <div className="rounded-2xl border border-dashed border-line bg-surface-muted px-4 py-8 text-center text-sm font-semibold text-ink-secondary">
      {message}
    </div>
  );
}

function AdminProjectCard({ project, report, members = [] }) {
  const progress = projectProgress(report, project);
  const status = projectStatus(project);
  const statusColor = status === 'Completed' ? 'text-emerald-700' : status === 'At Risk' ? 'text-red-700' : 'text-brand-secondary';
  const visibleMembers = members.slice(0, 4);

  return (
    <Card className="flex h-full flex-col p-5 transition duration-200 hover:-translate-y-0.5 hover:shadow-soft" interactive={false}>
      <div className="flex items-start justify-between gap-3">
        <div>
          <h3 className="text-lg font-extrabold text-ink-primary">{project.name || 'Untitled Project'}</h3>
          <p className={`mt-1 text-sm font-bold ${statusColor}`}>{status}</p>
        </div>
        <Badge>{project.priority || project.type || 'Normal'}</Badge>
      </div>
      <div className="mt-5">
        <div className="mb-2 flex items-center justify-between text-sm font-bold">
          <span className="text-ink-secondary">Progress</span>
          <span className="text-brand-primary">{progress}%</span>
        </div>
        <ProgressBar value={progress} />
      </div>
      <div className="mt-5 grid gap-3 sm:grid-cols-2">
        <div className="rounded-2xl border border-line p-3">
          <p className="text-xs font-bold uppercase text-ink-secondary">Project Manager</p>
          <p className="mt-1 text-sm font-bold text-ink-primary">{project.ownerName || project.managerName || 'Unassigned'}</p>
        </div>
        <div className="rounded-2xl border border-line p-3">
          <p className="text-xs font-bold uppercase text-ink-secondary">Deadline</p>
          <p className="mt-1 text-sm font-bold text-ink-primary">{formatDate(project.endDate || project.deadline)}</p>
        </div>
      </div>
      <div className="mt-5 flex-1">
        <p className="text-xs font-bold uppercase text-ink-secondary">Team Members</p>
        <div className="mt-3 flex flex-wrap gap-2">
          {visibleMembers.length === 0 && <span className="text-sm text-ink-secondary">No members loaded</span>}
          {visibleMembers.map((member) => (
            <span key={member.id || member.employeeId} className="rounded-full border border-line bg-white px-3 py-1 text-xs font-bold text-ink-primary">
              {member.employeeName || member.name || 'Team Member'}
            </span>
          ))}
          {members.length > visibleMembers.length && (
            <span className="rounded-full bg-brand-blueAccent px-3 py-1 text-xs font-bold text-brand-secondary">+{members.length - visibleMembers.length}</span>
          )}
        </div>
      </div>
    </Card>
  );
}

export default function AdminDashboard() {
  const [leaveStatus, setLeaveStatus] = useState('');
  const [announcementOpen, setAnnouncementOpen] = useState(false);
  const [editingAnnouncement, setEditingAnnouncement] = useState(null);
  const [localAnnouncements, setLocalAnnouncements] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem(ANNOUNCEMENTS_KEY) || '[]');
    } catch {
      return [];
    }
  });
  const [announcementForm, setAnnouncementForm] = useState({
    title: '',
    message: '',
    priority: 'Normal',
    publishDate: new Date().toISOString().slice(0, 10),
    expiryDate: '',
  });
  const { data: company } = useApiData(() => organizationApi.company(), null, []);
  const { data: attendance, loading: attendanceLoading } = useApiData(() => attendanceApi.dashboard(), null, []);
  const { data: employees, loading: employeesLoading } = useApiData(
    async () => pageContent(await employeesApi.list({ size: 500 })),
    [],
    [],
  );
  const { data: departments, loading: departmentsLoading } = useApiData(
    async () => (company?.id ? organizationApi.departments({ companyId: company.id }) : []),
    [],
    [company?.id],
  );
  const { data: tasks, loading: tasksLoading } = useApiData(
    async () => pageContent(await tasksApi.search({ size: 500 })),
    [],
    [],
  );
  const { data: pendingLeaves, loading: leavesLoading, refresh: refreshLeaves } = useApiData(
    () => leaveApi.pendingHr(),
    [],
    [],
  );
  const { data: leaveCalendar } = useApiData(
    () => {
      const now = new Date();
      const from = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;
      const to = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()).padStart(2, '0')}`;
      return leaveApi.calendar({ from, to });
    },
    [],
    [],
  );
  const { data: notifications, loading: notificationsLoading } = useApiData(
    async () => pageContent(await notificationsApi.list({ size: 12 })).map(mapNotification),
    [],
    [],
  );
  const { data: portfolio, loading: projectsLoading } = useApiData(
    async () => {
      if (!company?.id) return [];
      const projectList = pageContent(await projectsApi.list({ companyId: company.id, size: 9 }));
      return Promise.all(projectList.map(async (project) => {
        try {
          const [report, members] = await Promise.all([
            reportsApi.project(project.id),
            projectsApi.members(project.id),
          ]);
          return { project, report, members: Array.isArray(members) ? members : [] };
        } catch {
          return { project, report: null, members: [] };
        }
      }));
    },
    [],
    [company?.id],
  );

  const loading = attendanceLoading || employeesLoading || departmentsLoading || tasksLoading || leavesLoading || notificationsLoading || projectsLoading;

  const taskCounts = useMemo(() => {
    const completed = tasks.filter((task) => ['DONE', 'COMPLETED'].includes(taskStatus(task))).length;
    const inProgress = tasks.filter((task) => ['IN_PROGRESS', 'IN_REVIEW', 'TESTING'].includes(taskStatus(task))).length;
    const overdue = tasks.filter((task) => task.dueDate && new Date(task.dueDate) < new Date() && !['DONE', 'COMPLETED', 'CANCELLED'].includes(taskStatus(task))).length;
    const pending = Math.max(tasks.length - completed - inProgress, 0);
    return { total: tasks.length, completed, inProgress, overdue, pending };
  }, [tasks]);

  const activeProjects = portfolio.filter(({ project }) => !/complete|archived|cancel/i.test(project.status || '')).length;
  const newJoiners = employees.filter((employee) => isCurrentMonth(employee.dateOfJoining || employee.joiningDate)).length;
  const onLeave = attendance?.onLeave ?? leaveCalendar.length;

  const kpis = [
    { title: 'Total Employees', value: formatNumber(attendance?.totalEmployees ?? employees.length), icon: Users },
    { title: 'Employees Present Today', value: formatNumber(attendance?.present), icon: CheckCircle2, accent: 'bg-brand-successSoft text-emerald-700', trend: attendance?.presentPercentage !== undefined ? `${attendance.presentPercentage.toFixed(1)}% present` : '' },
    { title: 'Employees Absent', value: formatNumber(attendance?.absent), icon: CalendarClock, accent: 'bg-brand-warningSoft text-amber-700' },
    { title: 'Employees on Leave', value: formatNumber(onLeave), icon: ClipboardCheck, accent: 'bg-brand-blueAccent text-brand-secondary' },
    { title: 'Active Projects', value: formatNumber(activeProjects), icon: FolderKanban },
    { title: 'Open Tasks', value: formatNumber(taskCounts.pending + taskCounts.inProgress + taskCounts.overdue), icon: ClipboardList },
    { title: 'Departments', value: formatNumber(departments.length), icon: Building2 },
    { title: 'New Joiners This Month', value: formatNumber(newJoiners), icon: UserPlus, accent: 'bg-brand-successSoft text-emerald-700' },
  ];

  const apiAnnouncements = notifications.filter((note) => /announcement|system/i.test(note.type || note.raw?.type || '')).slice(0, 5);
  const announcements = [...localAnnouncements, ...apiAnnouncements].slice(0, 8);

  function persistAnnouncements(items) {
    setLocalAnnouncements(items);
    localStorage.setItem(ANNOUNCEMENTS_KEY, JSON.stringify(items));
  }

  function openAnnouncementForm(item = null) {
    setEditingAnnouncement(item?.local ? item : null);
    setAnnouncementForm(item?.local ? {
      title: item.title,
      message: item.message,
      priority: item.priority,
      publishDate: item.publishDate,
      expiryDate: item.expiryDate || '',
    } : {
      title: '',
      message: '',
      priority: 'Normal',
      publishDate: new Date().toISOString().slice(0, 10),
      expiryDate: '',
    });
    setAnnouncementOpen(true);
  }

  function saveAnnouncement() {
    if (!announcementForm.title.trim() || !announcementForm.message.trim()) return;
    const nextItem = {
      id: editingAnnouncement?.id || `announcement-${Date.now()}`,
      local: true,
      title: announcementForm.title.trim(),
      message: announcementForm.message.trim(),
      priority: announcementForm.priority,
      publishDate: announcementForm.publishDate,
      expiryDate: announcementForm.expiryDate,
      time: new Date(announcementForm.publishDate).toLocaleDateString(),
    };
    const next = editingAnnouncement
      ? localAnnouncements.map((item) => (item.id === editingAnnouncement.id ? nextItem : item))
      : [nextItem, ...localAnnouncements];
    persistAnnouncements(next);
    setAnnouncementOpen(false);
    setEditingAnnouncement(null);
  }

  function deleteAnnouncement(id) {
    persistAnnouncements(localAnnouncements.filter((item) => item.id !== id));
  }

  async function decideLeave(leave, decision) {
    setLeaveStatus(`${titleCase(decision)} leave request...`);
    try {
      await leaveApi.approveHr({ leaveRequestId: leave.id, decision, remarks: `Marked ${decision.toLowerCase()} from admin dashboard` });
      setLeaveStatus(`Leave request ${decision.toLowerCase()}.`);
      refreshLeaves();
    } catch (err) {
      setLeaveStatus(err.message);
    }
  }

  return (
    <div className="page-stack">
      <PageHeader
        kicker="Company Command Center"
        title={buildWelcomeTitle('Admin')}
        description="Here's your company's operational overview for today."
        actions={<Link to="/reports"><Button variant="blue"><FileBarChart className="h-4 w-4" /> View Analytics</Button></Link>}
      />

      {loading && <LoadingIndicator message="Loading company command center..." />}
      {leaveStatus && <p className="alert-info" role="status">{leaveStatus}</p>}

      <section className="card-grid sm:grid-cols-2 xl:grid-cols-4">
        {kpis.map((kpi) => <KpiCard key={kpi.title} {...kpi} />)}
      </section>

      <section className="space-y-4">
        <div className="flex items-end justify-between gap-4">
          <div>
            <h2 className="section-title">Project Portfolio</h2>
            <p className="mt-1 text-sm text-ink-secondary">Project manager, team, delivery status, deadline, priority, and progress.</p>
          </div>
          <Link to="/projects" className="text-sm font-bold text-brand-primary hover:underline">View all projects</Link>
        </div>
        {portfolio.length === 0 ? (
          <Card className="p-5" interactive={false}><EmptyState message="No projects available from the projects API." /></Card>
        ) : (
          <div className="card-grid md:grid-cols-2 xl:grid-cols-3">
            {portfolio.map(({ project, report, members }) => (
              <AdminProjectCard key={project.id} project={project} report={report} members={members} />
            ))}
          </div>
        )}
      </section>

      <section>
        <Card className="overflow-hidden" interactive={false}>
          <div className="border-b border-line p-5">
            <h2 className="section-title">Pending Leave Requests</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="sticky top-0 bg-surface-muted text-xs uppercase text-ink-secondary">
                <tr>
                  {['Employee', 'Department', 'Leave Dates', 'Type', 'Reason', 'Actions'].map((head) => <th key={head} className="px-5 py-4">{head}</th>)}
                </tr>
              </thead>
              <tbody>
                {pendingLeaves.length === 0 && (
                  <tr><td colSpan={6} className="px-5 py-8 text-center text-ink-secondary">No pending HR leave requests.</td></tr>
                )}
                {pendingLeaves.slice(0, 6).map((leave) => (
                  <tr key={leave.id} className="border-t border-line">
                    <td className="px-5 py-4 font-bold text-ink-primary">{leave.employeeName || leave.employee?.name || '-'}</td>
                    <td className="px-5 py-4 text-ink-secondary">{leave.departmentName || leave.employee?.departmentName || '-'}</td>
                    <td className="px-5 py-4 text-ink-secondary">{formatDate(leave.startDate)} - {formatDate(leave.endDate)}</td>
                    <td className="px-5 py-4"><Badge>{leave.leaveTypeName || leave.leaveType || 'Leave'}</Badge></td>
                    <td className="max-w-56 px-5 py-4 text-ink-secondary">{leave.reason || '-'}</td>
                    <td className="px-5 py-4">
                      <div className="flex gap-2">
                        <Button className="min-h-9 px-3 py-1.5" onClick={() => decideLeave(leave, 'APPROVED')}>Approve</Button>
                        <Button variant="secondary" className="min-h-9 px-3 py-1.5" onClick={() => decideLeave(leave, 'REJECTED')}>Reject</Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </section>

      <section>
        <Card className="p-5" interactive={false}>
          <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
            <h2 className="section-title flex items-center gap-2"><Megaphone className="h-5 w-5 text-brand-primary" /> Company Announcements</h2>
            <Button onClick={() => openAnnouncementForm()}><Plus className="h-4 w-4" /> Add Announcement</Button>
          </div>
          <div className="mt-4 space-y-3">
            {announcements.length === 0 && <EmptyState message="No company announcements returned by notifications API." />}
            {announcements.map((item) => (
              <div key={item.id} className="rounded-2xl border border-line bg-white px-4 py-3">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-bold text-ink-primary">{item.title}</p>
                    <p className="mt-1 text-sm text-ink-secondary">{item.message}</p>
                    <p className="mt-2 text-xs font-semibold text-ink-secondary">Posted {item.publishDate ? formatDate(item.publishDate) : item.time}</p>
                  </div>
                  <div className="flex shrink-0 items-center gap-2">
                    <Badge>{item.priority || item.raw?.priority || item.type || 'Info'}</Badge>
                    {item.local && (
                      <>
                        <Button variant="secondary" className="min-h-9 px-3 py-1.5" onClick={() => openAnnouncementForm(item)} aria-label={`Edit ${item.title}`}><Edit3 className="h-4 w-4" /></Button>
                        <Button variant="secondary" className="min-h-9 px-3 py-1.5" onClick={() => deleteAnnouncement(item.id)} aria-label={`Delete ${item.title}`}><Trash2 className="h-4 w-4" /></Button>
                      </>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </section>

      <Modal
        open={announcementOpen}
        onClose={() => setAnnouncementOpen(false)}
        title={editingAnnouncement ? 'Edit Announcement' : 'Add Announcement'}
        description="Create company announcements for employees and operations teams."
        footer={<ModalActions onCancel={() => setAnnouncementOpen(false)} onConfirm={saveAnnouncement} confirmLabel="Save Announcement" />}
      >
        <div className="grid gap-4 md:grid-cols-2">
          <label className="block md:col-span-2">
            <span className="text-sm font-semibold text-ink-primary">Announcement Title</span>
            <input className="field-control mt-2" value={announcementForm.title} onChange={(event) => setAnnouncementForm((current) => ({ ...current, title: event.target.value }))} />
          </label>
          <label className="block md:col-span-2">
            <span className="text-sm font-semibold text-ink-primary">Description</span>
            <textarea className="field-control mt-2 min-h-28" value={announcementForm.message} onChange={(event) => setAnnouncementForm((current) => ({ ...current, message: event.target.value }))} />
          </label>
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">Priority</span>
            <select className="select-control mt-2" value={announcementForm.priority} onChange={(event) => setAnnouncementForm((current) => ({ ...current, priority: event.target.value }))}>
              <option>Normal</option>
              <option>Important</option>
              <option>Urgent</option>
            </select>
          </label>
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">Publish Date</span>
            <input type="date" className="field-control mt-2" value={announcementForm.publishDate} onChange={(event) => setAnnouncementForm((current) => ({ ...current, publishDate: event.target.value }))} />
          </label>
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">Expiry Date</span>
            <input type="date" className="field-control mt-2" value={announcementForm.expiryDate} onChange={(event) => setAnnouncementForm((current) => ({ ...current, expiryDate: event.target.value }))} />
          </label>
        </div>
      </Modal>

      <Card className="p-5" interactive={false}>
        <h2 className="section-title">Quick Actions</h2>
        <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {[
            ['Add Employee', '/employees', UserPlus],
            ['Create Department', '/departments', Building2],
            ['Create Project', '/projects', FolderKanban],
            ['Assign Task', '/tasks', ClipboardList],
            ['Create Announcement', '/notifications', Megaphone],
            ['Generate Report', '/reports', FileBarChart],
            ['Approve Leaves', '/leave', ClipboardCheck],
            ['View Analytics', '/reports', TrendingUp],
          ].map(([label, path, Icon]) => (
            <Link key={label} to={path} className="group flex items-center gap-3 rounded-2xl border border-line bg-white p-4 font-bold text-ink-primary shadow-sm transition duration-200 hover:-translate-y-0.5 hover:bg-brand-blueAccent hover:text-brand-secondary hover:shadow-soft">
              <span className="grid h-10 w-10 place-items-center rounded-xl bg-brand-blueAccent text-brand-secondary transition group-hover:bg-white"><Icon className="h-5 w-5" /></span>
              {label}
            </Link>
          ))}
        </div>
      </Card>
    </div>
  );
}
