import React, { useMemo, useState } from 'react';
import Modal, { ModalActions } from '../ui/Modal';
import { employeesApi, organizationApi, pageContent, projectsApi, tasksApi } from '../../lib/api';

const ASSIGNMENT_MODES = [
  { id: 'single', label: 'Single Employee' },
  { id: 'multiple', label: 'Multiple Employees' },
  { id: 'team', label: 'Entire Team' },
  { id: 'department', label: 'Entire Department' },
];

export default function AssignTaskModal({ open, onClose, onAssigned, companyId }) {
  const [mode, setMode] = useState('single');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [teams, setTeams] = useState([]);
  const [projects, setProjects] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [selectedTeamId, setSelectedTeamId] = useState('');
  const [selectedDepartmentId, setSelectedDepartmentId] = useState('');
  const [attachments, setAttachments] = useState([]);
  const [form, setForm] = useState({
    projectId: '',
    title: '',
    description: '',
    priority: 'MEDIUM',
    dueDate: '',
  });

  React.useEffect(() => {
    if (!open) return;
    async function load() {
      try {
        const [employeePage, departmentList, projectPage] = await Promise.all([
          employeesApi.list({ size: 200 }),
          companyId ? organizationApi.departments({ companyId }) : Promise.resolve([]),
          companyId ? projectsApi.list({ companyId, size: 50 }) : Promise.resolve({ content: [] }),
        ]);
        setEmployees(pageContent(employeePage));
        setDepartments(Array.isArray(departmentList) ? departmentList : []);
        setProjects(pageContent(projectPage));
      } catch {
        setEmployees([]);
      }
    }
    load();
  }, [open, companyId]);

  React.useEffect(() => {
    if (!selectedDepartmentId) {
      setTeams([]);
      return;
    }
    organizationApi.teams(selectedDepartmentId).then(setTeams).catch(() => setTeams([]));
  }, [selectedDepartmentId]);

  const filteredEmployees = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return employees;
    return employees.filter((employee) => {
      const name = `${employee.firstName || ''} ${employee.lastName || ''}`.trim().toLowerCase();
      return name.includes(term) || employee.email?.toLowerCase().includes(term) || employee.employeeId?.toLowerCase().includes(term);
    });
  }, [employees, search]);
  const selectedEmployee = employees.find((employee) => employee.id === selectedIds[0]);

  function toggleEmployee(id) {
    setSelectedIds((current) => (current.includes(id) ? current.filter((value) => value !== id) : [...current, id]));
  }

  async function resolveAssigneeIds() {
    if (mode === 'single' || mode === 'multiple') return selectedIds;
    if (mode === 'team' && selectedTeamId) {
      return employees.filter((employee) => employee.teamId === selectedTeamId).map((employee) => employee.id);
    }
    if (mode === 'department' && selectedDepartmentId) {
      return employees.filter((employee) => employee.departmentId === selectedDepartmentId).map((employee) => employee.id);
    }
    return [];
  }

  async function handleAssign() {
    setError('');
    if (!form.projectId || !form.title.trim()) {
      setError('Project and task name are required.');
      return;
    }
    const assigneeIds = await resolveAssigneeIds();
    if (assigneeIds.length === 0) {
      setError('Select at least one assignee.');
      return;
    }
    setLoading(true);
    try {
      await Promise.all(
        assigneeIds.map((assigneeId) => tasksApi.create({
          projectId: form.projectId,
          title: form.title,
          description: form.description,
          priority: form.priority,
          dueDate: form.dueDate || undefined,
          assigneeId,
          status: 'TODO',
        })),
      );
      onAssigned?.(assigneeIds.length);
      onClose?.();
      setForm({ projectId: '', title: '', description: '', priority: 'MEDIUM', dueDate: '' });
      setSelectedIds([]);
      setAttachments([]);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Assign Task"
      description="Assign work to individuals, multiple employees, a team, or an entire department."
      size="xl"
      footer={<ModalActions onCancel={onClose} onConfirm={handleAssign} confirmLabel={loading ? 'Assigning...' : 'Assign Task'} loading={loading} />}
    >
      <div className="grid gap-5">
        {error && <p className="alert-warning" role="alert">{error}</p>}

        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {ASSIGNMENT_MODES.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => setMode(item.id)}
              className={`rounded-2xl border px-4 py-3 text-left text-sm font-bold transition ${
                mode === item.id ? 'border-brand-accent bg-brand-redSoft text-brand-accent' : 'border-line text-ink-primary hover:bg-surface-muted'
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <label className="block md:col-span-2">
            <span className="text-sm font-semibold text-ink-primary">Project</span>
            <select className="select-control mt-2" value={form.projectId} onChange={(event) => setForm((current) => ({ ...current, projectId: event.target.value }))} required>
              <option value="">Select project</option>
              {projects.map((project) => (
                <option key={project.id} value={project.id}>{project.name}</option>
              ))}
            </select>
          </label>
          <label className="block md:col-span-2">
            <span className="text-sm font-semibold text-ink-primary">Task Name</span>
            <input className="field-control mt-2" value={form.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} required />
          </label>
          <label className="block md:col-span-2">
            <span className="text-sm font-semibold text-ink-primary">Description</span>
            <textarea className="field-control mt-2 min-h-24" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} />
          </label>
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">Priority</span>
            <select className="select-control mt-2" value={form.priority} onChange={(event) => setForm((current) => ({ ...current, priority: event.target.value }))}>
              <option>CRITICAL</option>
              <option>HIGH</option>
              <option>MEDIUM</option>
              <option>LOW</option>
            </select>
          </label>
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">Deadline</span>
            <input type="date" className="field-control mt-2" value={form.dueDate} onChange={(event) => setForm((current) => ({ ...current, dueDate: event.target.value }))} />
          </label>
          <label className="block md:col-span-2">
            <span className="text-sm font-semibold text-ink-primary">Attachments</span>
            <input type="file" multiple className="field-control mt-2" onChange={(event) => setAttachments(Array.from(event.target.files || []))} />
            {attachments.length > 0 && (
              <p className="mt-2 text-xs text-ink-secondary">{attachments.length} file(s) ready — upload will be available when document API supports task attachments.</p>
            )}
          </label>
        </div>

        {(mode === 'single' || mode === 'multiple') && (
          <div>
            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">Employee Name / Employee ID</span>
              <input className="field-control mt-2" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by name, email, or employee ID" />
            </label>
            <div className="mt-3 max-h-56 space-y-2 overflow-y-auto no-scrollbar rounded-2xl border border-line p-3">
              {filteredEmployees.map((employee) => {
                const name = `${employee.firstName || ''} ${employee.lastName || ''}`.trim() || employee.email;
                const checked = selectedIds.includes(employee.id);
                return (
                  <label key={employee.id} className="flex cursor-pointer items-center gap-3 rounded-xl px-3 py-2 hover:bg-surface-muted">
                    <input
                      type={mode === 'single' ? 'radio' : 'checkbox'}
                      name="assignee"
                      checked={checked}
                      onChange={() => (mode === 'single' ? setSelectedIds([employee.id]) : toggleEmployee(employee.id))}
                    />
                    <span className="text-sm font-semibold text-ink-primary">{name}</span>
                    <span className="text-xs text-ink-secondary">{employee.departmentName || employee.designationName}</span>
                  </label>
                );
              })}
            </div>
            {selectedEmployee && (
              <div className="mt-3 grid gap-3 rounded-2xl bg-surface-muted p-4 sm:grid-cols-4">
                <div><p className="text-xs font-bold uppercase text-ink-secondary">Employee Name</p><p className="mt-1 text-sm font-bold text-ink-primary">{`${selectedEmployee.firstName || ''} ${selectedEmployee.lastName || ''}`.trim() || selectedEmployee.email}</p></div>
                <div><p className="text-xs font-bold uppercase text-ink-secondary">Employee ID</p><p className="mt-1 text-sm font-bold text-ink-primary">{selectedEmployee.employeeId || '-'}</p></div>
                <div><p className="text-xs font-bold uppercase text-ink-secondary">Department</p><p className="mt-1 text-sm font-bold text-ink-primary">{selectedEmployee.departmentName || '-'}</p></div>
                <div><p className="text-xs font-bold uppercase text-ink-secondary">Designation</p><p className="mt-1 text-sm font-bold text-ink-primary">{selectedEmployee.designationName || selectedEmployee.employmentType || '-'}</p></div>
              </div>
            )}
          </div>
        )}

        {mode === 'team' && (
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">Department</span>
            <select className="select-control mt-2" value={selectedDepartmentId} onChange={(event) => setSelectedDepartmentId(event.target.value)}>
              <option value="">Select department</option>
              {departments.map((department) => (
                <option key={department.id} value={department.id}>{department.name}</option>
              ))}
            </select>
            <span className="mt-4 block text-sm font-semibold text-ink-primary">Team</span>
            <select className="select-control mt-2" value={selectedTeamId} onChange={(event) => setSelectedTeamId(event.target.value)}>
              <option value="">Select team</option>
              {teams.map((team) => (
                <option key={team.id} value={team.id}>{team.name}</option>
              ))}
            </select>
          </label>
        )}

        {mode === 'department' && (
          <label className="block">
            <span className="text-sm font-semibold text-ink-primary">Department</span>
            <select className="select-control mt-2" value={selectedDepartmentId} onChange={(event) => setSelectedDepartmentId(event.target.value)}>
              <option value="">Select department</option>
              {departments.map((department) => (
                <option key={department.id} value={department.id}>{department.name}</option>
              ))}
            </select>
          </label>
        )}
      </div>
    </Modal>
  );
}
