import React, { useMemo, useState } from 'react';
import { Building2, Paperclip, UserRoundCheck } from 'lucide-react';
import Modal, { ModalActions } from '../ui/Modal';
import Button from '../ui/Button';
import { documentsApi, employeesApi, organizationApi, pageContent, projectsApi, tasksApi } from '../../lib/api';

const ASSIGNMENT_MODES = [
  { id: 'single', label: 'Single Employee' },
  { id: 'multiple', label: 'Multiple Employees' },
  { id: 'team', label: 'Entire Team' },
  { id: 'department', label: 'Entire Department' },
];

export default function AssignTaskModal({ open, onClose, onAssigned, companyId }) {
  const fileInputRef = React.useRef(null);
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

  const selectedProject = projects.find((project) => project.id === form.projectId);
  const selectedDepartment = departments.find((department) => department.id === selectedDepartmentId);

  const filteredEmployees = useMemo(() => {
    const term = search.trim().toLowerCase();
    return employees.filter((employee) => {
      if (selectedDepartmentId && employee.departmentId !== selectedDepartmentId) return false;
      if (!term) return true;
      const name = `${employee.firstName || ''} ${employee.lastName || ''}`.trim().toLowerCase();
      return name.includes(term) || employee.email?.toLowerCase().includes(term) || employee.employeeId?.toLowerCase().includes(term);
    });
  }, [employees, search, selectedDepartmentId]);
  const selectedEmployee = employees.find((employee) => employee.id === selectedIds[0]);
  const selectedAssignees = employees.filter((employee) => selectedIds.includes(employee.id));
  const assignmentCount = mode === 'team'
    ? employees.filter((employee) => employee.teamId === selectedTeamId).length
    : mode === 'department'
      ? employees.filter((employee) => employee.departmentId === selectedDepartmentId).length
      : selectedIds.length;

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
    if (!form.projectId || !selectedDepartmentId || !form.title.trim()) {
      setError('Project, project department, and task name are required.');
      return;
    }
    const assigneeIds = await resolveAssigneeIds();
    if (assigneeIds.length === 0) {
      setError('Select at least one assignee.');
      return;
    }
    setLoading(true);
    try {
      const createdTasks = await Promise.all(
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
      if (attachments.length > 0) {
        await Promise.all(
          createdTasks.flatMap((task) => attachments.map(async (file) => {
            const uploaded = await documentsApi.uploadTaskAttachment(task.id, file);
            await tasksApi.addComment(task.id, `Attachment uploaded: ${uploaded.fileName || file.name} - ${uploaded.fileUrl}`);
          })),
        );
      }
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
              onClick={() => {
                setMode(item.id);
                setSelectedIds([]);
                setSelectedTeamId('');
                setSearch('');
              }}
              className={`rounded-2xl border px-4 py-3 text-left text-sm font-bold transition ${
                mode === item.id ? 'border-brand-accent bg-brand-redSoft text-brand-accent' : 'border-line text-ink-primary hover:bg-surface-muted'
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>

        <section className="rounded-2xl border border-line bg-surface-muted/40 p-4">
          <div className="mb-4 flex items-center gap-2">
            <span className="grid h-9 w-9 place-items-center rounded-xl bg-white text-brand-primary shadow-soft">
              <UserRoundCheck className="h-4 w-4" />
            </span>
            <div>
              <h3 className="text-sm font-extrabold text-ink-primary">Assignment Details</h3>
              <p className="text-xs font-semibold text-ink-secondary">Choose the project department and employee ID before creating the work item.</p>
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">Project</span>
              <select className="select-control mt-2" value={form.projectId} onChange={(event) => setForm((current) => ({ ...current, projectId: event.target.value }))} required>
                <option value="">Select project</option>
                {projects.map((project) => (
                  <option key={project.id} value={project.id}>{project.name}</option>
                ))}
              </select>
            </label>

            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">Project Department</span>
              <select
                className="select-control mt-2"
                value={selectedDepartmentId}
                onChange={(event) => {
                  setSelectedDepartmentId(event.target.value);
                  setSelectedIds([]);
                  setSelectedTeamId('');
                }}
                required
              >
                <option value="">Select department</option>
                {departments.map((department) => (
                  <option key={department.id} value={department.id}>{department.name}</option>
                ))}
              </select>
            </label>

            {(mode === 'single' || mode === 'multiple') && (
              <div className="md:col-span-2">
                <label className="block">
                  <span className="text-sm font-semibold text-ink-primary">Employee Name / Employee ID</span>
                  <input className="field-control mt-2" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by name, email, or employee ID" />
                </label>
                <div className="mt-3 max-h-56 space-y-2 overflow-y-auto no-scrollbar rounded-2xl border border-line bg-white p-3">
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
                        <span className="grid flex-1 gap-1 text-sm sm:grid-cols-3">
                          <span className="font-semibold text-ink-primary">{name}</span>
                          <span className="text-ink-secondary">ID {employee.employeeId || '-'}</span>
                          <span className="text-ink-secondary">{employee.departmentName || '-'}</span>
                        </span>
                      </label>
                    );
                  })}
                  {filteredEmployees.length === 0 && (
                    <p className="px-3 py-2 text-sm text-ink-secondary">No employees found for this department.</p>
                  )}
                </div>
              </div>
            )}

            {mode === 'team' && (
              <label className="block md:col-span-2">
                <span className="text-sm font-semibold text-ink-primary">Team</span>
                <select className="select-control mt-2" value={selectedTeamId} onChange={(event) => setSelectedTeamId(event.target.value)}>
                  <option value="">Select team</option>
                  {teams.map((team) => (
                    <option key={team.id} value={team.id}>{team.name}</option>
                  ))}
                </select>
              </label>
            )}

            <div className="grid gap-3 rounded-2xl bg-white p-4 md:col-span-2 sm:grid-cols-3">
              <div>
                <p className="text-xs font-bold uppercase text-ink-secondary">Project</p>
                <p className="mt-1 text-sm font-bold text-ink-primary">{selectedProject?.name || 'Not selected'}</p>
              </div>
              <div>
                <p className="text-xs font-bold uppercase text-ink-secondary">Department</p>
                <p className="mt-1 flex items-center gap-2 text-sm font-bold text-ink-primary"><Building2 className="h-4 w-4 text-brand-primary" /> {selectedDepartment?.name || 'Not selected'}</p>
              </div>
              <div>
                <p className="text-xs font-bold uppercase text-ink-secondary">Assigned Employees</p>
                <p className="mt-1 text-sm font-bold text-ink-primary">{assignmentCount || 'None selected'}</p>
              </div>
            </div>

            {selectedAssignees.length > 0 && (
              <div className="rounded-2xl bg-white p-4 md:col-span-2">
                <p className="text-xs font-bold uppercase text-ink-secondary">Selected Employee ID{selectedAssignees.length > 1 ? 's' : ''}</p>
                <p className="mt-2 text-sm font-bold text-ink-primary">
                  {selectedAssignees.map((employee) => `${employee.employeeId || '-'} - ${`${employee.firstName || ''} ${employee.lastName || ''}`.trim() || employee.email}`).join(', ')}
                </p>
              </div>
            )}
          </div>
        </section>

        <div className="grid gap-4 md:grid-cols-2">
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
            <div className="mt-2 flex flex-wrap items-center gap-3">
              <Button type="button" variant="secondary" onClick={() => fileInputRef.current?.click()}>
                <Paperclip className="h-4 w-4" /> Attach / Upload File
              </Button>
              <span className="text-xs text-ink-secondary">PDF or any other file</span>
            </div>
            <input ref={fileInputRef} type="file" multiple className="hidden" onChange={(event) => setAttachments(Array.from(event.target.files || []))} />
            {attachments.length > 0 && (
              <p className="mt-2 text-xs text-ink-secondary">{attachments.map((file) => file.name).join(', ')}</p>
            )}
          </label>
        </div>

        {selectedEmployee && (
          <div className="grid gap-3 rounded-2xl bg-surface-muted p-4 sm:grid-cols-3">
            <div><p className="text-xs font-bold uppercase text-ink-secondary">Employee Name</p><p className="mt-1 text-sm font-bold text-ink-primary">{`${selectedEmployee.firstName || ''} ${selectedEmployee.lastName || ''}`.trim() || selectedEmployee.email}</p></div>
            <div><p className="text-xs font-bold uppercase text-ink-secondary">Employee ID</p><p className="mt-1 text-sm font-bold text-ink-primary">{selectedEmployee.employeeId || '-'}</p></div>
            <div><p className="text-xs font-bold uppercase text-ink-secondary">Department</p><p className="mt-1 text-sm font-bold text-ink-primary">{selectedEmployee.departmentName || '-'}</p></div>
          </div>
        )}
      </div>
    </Modal>
  );
}

