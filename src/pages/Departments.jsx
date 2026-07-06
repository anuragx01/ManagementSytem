import React, { useMemo } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, Building2, FolderKanban, Users } from 'lucide-react';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import ProgressBar from '../components/ui/ProgressBar';
import { pageContent } from '../services/baseApi';
import { useGetEmployeesQuery } from '../services/employeeApi';
import { useGetCompanyQuery, useGetDepartmentsQuery } from '../services/organizationApi';
import { useGetProjectsQuery } from '../services/projectApi';
import { useSearchTasksQuery } from '../services/taskApi';
import { mapEmployee, mapTask } from '../lib/mappers';

function taskStatus(task) {
  return task.raw?.status || task.status || 'TODO';
}

function taskAssigneeId(task) {
  return task.raw?.assigneeId || task.assigneeId;
}

export default function Departments() {
  const { departmentId } = useParams();
  const { data: company } = useGetCompanyQuery();
  const { data: departments = [], isLoading: loading, error } = useGetDepartmentsQuery({ companyId: company?.id }, { skip: !company?.id });
  const { data: employeePage } = useGetEmployeesQuery({ size: 500 });
  const { data: projectPage } = useGetProjectsQuery({ companyId: company?.id, size: 100 }, { skip: !company?.id });
  const { data: taskPage } = useSearchTasksQuery({ size: 500 });
  const employees = pageContent(employeePage).map(mapEmployee);
  const projects = pageContent(projectPage);
  const tasks = pageContent(taskPage).map(mapTask);

  const selectedDepartment = departments.find((department) => department.id === departmentId);
  const departmentRows = useMemo(() => departments.map((department) => {
    const members = employees.filter((employee) => employee.raw?.departmentId === department.id || employee.department === department.name);
    const activeProjects = projects.filter((project) => project.departmentId === department.id || project.departmentName === department.name);
    return { ...department, members, activeProjects };
  }), [departments, employees, projects]);

  const detailMembers = employees.filter((employee) => employee.raw?.departmentId === departmentId || employee.department === selectedDepartment?.name);
  const detailProjects = projects.filter((project) => project.departmentId === departmentId || project.departmentName === selectedDepartment?.name);
  const completedProjects = detailProjects.filter((project) => /done|complete/i.test(project.status || '')).length;

  if (departmentId) {
    return (
      <div className="page-stack">
        <PageHeader
          kicker="Department Details"
          title={selectedDepartment?.name || 'Department'}
          description="Department summary, people, projects, and current responsibilities."
          actions={<Link to="/departments" className="text-sm font-bold text-brand-primary hover:underline"><ArrowLeft className="mr-2 inline h-4 w-4" />Back to Departments</Link>}
        />
        {loading && <p className="loading-text">Loading department...</p>}
        {error && <p className="alert-warning" role="alert">{error.message || "Unable to load departments."}</p>}
        <section className="card-grid sm:grid-cols-2 xl:grid-cols-4">
          <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">Total Employees</p><p className="mt-2 text-3xl font-extrabold">{detailMembers.length}</p></Card>
          <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">Active Projects</p><p className="mt-2 text-3xl font-extrabold">{detailProjects.length}</p></Card>
          <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">Completed Projects</p><p className="mt-2 text-3xl font-extrabold">{completedProjects}</p></Card>
          <Card className="p-5" interactive={false}><p className="text-sm text-ink-secondary">Department Head</p><p className="mt-2 text-xl font-extrabold">{selectedDepartment?.departmentHeadName || selectedDepartment?.headName || '-'}</p></Card>
        </section>
        <Card className="overflow-hidden" interactive={false}>
          <div className="border-b border-line p-5">
            <h2 className="section-title">Employee List</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-surface-muted text-xs uppercase text-ink-secondary">
                <tr>{['Employee', 'Employee ID', 'Designation', 'Current Project', 'Assigned Tasks', 'Task Status'].map((head) => <th key={head} className="px-5 py-4">{head}</th>)}</tr>
              </thead>
              <tbody>
                {detailMembers.length === 0 && <tr><td colSpan={6} className="px-5 py-8 text-center text-ink-secondary">No employees found in this department.</td></tr>}
                {detailMembers.map((employee) => {
                  const employeeTasks = tasks.filter((task) => taskAssigneeId(task) === employee.id);
                  const currentProject = projects.find((project) => project.id === employeeTasks[0]?.raw?.projectId);
                  const done = employeeTasks.filter((task) => ['DONE', 'COMPLETED'].includes(taskStatus(task))).length;
                  const progress = employeeTasks.length ? Math.round((done / employeeTasks.length) * 100) : 0;
                  return (
                    <tr key={employee.id} className="border-t border-line">
                      <td className="px-5 py-4 font-bold text-ink-primary">{employee.name}</td>
                      <td className="px-5 py-4 text-ink-secondary">{employee.raw?.employeeId || '-'}</td>
                      <td className="px-5 py-4 text-ink-secondary">{employee.role}</td>
                      <td className="px-5 py-4 text-ink-secondary">{currentProject?.name || '-'}</td>
                      <td className="px-5 py-4 text-ink-secondary">{employeeTasks.length}</td>
                      <td className="px-5 py-4"><div className="min-w-28"><ProgressBar value={progress} /></div></td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="page-stack">
      <PageHeader kicker="Organization" title="Departments" description="Department structure, headcount, projects, and ownership." />
      {loading && <p className="loading-text">Loading departments...</p>}
      {error && <p className="alert-warning" role="alert">{error.message || "Unable to load departments."}</p>}
      <section className="card-grid md:grid-cols-2 xl:grid-cols-3">
        {departmentRows.length === 0 && <Card className="p-5 md:col-span-2 xl:col-span-3" interactive={false}><p className="text-sm text-ink-secondary">No departments available.</p></Card>}
        {departmentRows.map((department) => (
          <Link key={department.id} to={`/departments/${department.id}`}>
            <Card className="h-full p-5 transition duration-200 hover:-translate-y-0.5 hover:shadow-soft">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 className="section-title">{department.name}</h2>
                  <p className="mt-1 text-sm text-ink-secondary">{department.description || department.code || 'Department'}</p>
                </div>
                <Badge>{department.active === false ? 'Inactive' : 'Active'}</Badge>
              </div>
              <div className="mt-5 grid gap-3 sm:grid-cols-3">
                <div className="rounded-2xl bg-surface-muted p-3"><Users className="h-4 w-4 text-brand-primary" /><p className="mt-2 text-xs text-ink-secondary">Employees</p><p className="font-bold">{department.members.length}</p></div>
                <div className="rounded-2xl bg-surface-muted p-3"><FolderKanban className="h-4 w-4 text-brand-primary" /><p className="mt-2 text-xs text-ink-secondary">Projects</p><p className="font-bold">{department.activeProjects.length}</p></div>
                <div className="rounded-2xl bg-surface-muted p-3"><Building2 className="h-4 w-4 text-brand-primary" /><p className="mt-2 text-xs text-ink-secondary">Head</p><p className="truncate font-bold">{department.departmentHeadName || department.headName || '-'}</p></div>
              </div>
            </Card>
          </Link>
        ))}
      </section>
    </div>
  );
}



