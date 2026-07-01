import React from "react";
import { useState } from 'react';
import { Mail, MessageSquare, Pencil, Phone, Plus, Trash2 } from 'lucide-react';
import { Can } from '../components/RoleGate';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import SearchFilter from '../components/ui/SearchFilter';
import { employees } from '../data/mockData';
import useApiData from '../hooks/useApiData';
import { employeesApi, organizationApi, pageContent } from '../lib/api';
import { mapEmployee } from '../lib/mappers';

export default function TeamDirectory() {
  const [showEmployeeForm, setShowEmployeeForm] = useState(false);
  const [showDepartmentForm, setShowDepartmentForm] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const [formStatus, setFormStatus] = useState('');
  const [employeeForm, setEmployeeForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    dateOfJoining: '',
    employmentType: 'FULL_TIME',
    workLocation: 'HYBRID',
  });
  const [departmentForm, setDepartmentForm] = useState({
    companyId: '',
    name: '',
    code: '',
    description: '',
    active: true,
  });
  const { data: teamMembers, loading, error } = useApiData(
    async () => pageContent(await employeesApi.list({ size: 50 })).map(mapEmployee),
    employees,
    [],
  );

  async function submitEmployee(event) {
    event.preventDefault();
    setFormStatus('Creating employee...');
    try {
      await employeesApi.create(employeeForm);
      setFormStatus('Employee created successfully.');
      setShowEmployeeForm(false);
    } catch (err) {
      setFormStatus(`Employee create failed: ${err.message}`);
    }
  }

  async function submitDepartment(event) {
    event.preventDefault();
    setFormStatus('Creating department...');
    try {
      await organizationApi.createDepartment(departmentForm);
      setFormStatus('Department created successfully.');
      setShowDepartmentForm(false);
    } catch (err) {
      setFormStatus(`Department create failed: ${err.message}`);
    }
  }

  async function submitEmployeeEdit(event) {
    event.preventDefault();
    setFormStatus('Updating employee...');
    try {
      await employeesApi.update(editingEmployee.id, editingEmployee.raw || {});
      setFormStatus('Employee updated successfully.');
      setEditingEmployee(null);
    } catch (err) {
      setFormStatus(`Employee update failed: ${err.message}`);
    }
  }

  async function deleteEmployee(employee) {
    setFormStatus('Deleting employee...');
    try {
      await employeesApi.terminate(employee.id, 'Deleted from admin UI');
      setFormStatus('Employee deleted/terminated successfully.');
    } catch (err) {
      setFormStatus(`Employee delete failed: ${err.message}`);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-bold text-brand-primary">Team Directory</p>
        <h1 className="mt-1 text-3xl font-extrabold text-ink-primary">People across NEXSTAR</h1>
      </div>
      <SearchFilter placeholder="Search team members" filterLabel="All Departments" options={['Engineering', 'HR', 'Finance', 'Operations', 'Support']} />
      {error && <p className="rounded-2xl bg-orange-50 px-4 py-3 text-sm font-semibold text-orange-700">Showing mock employees because API is unavailable: {error}</p>}
      {loading && <p className="text-sm font-semibold text-ink-secondary">Loading employees from backend...</p>}
      <Can roles={['admin', 'hr']}>
        <Card className="flex flex-col justify-between gap-4 p-5 sm:flex-row sm:items-center">
          <div>
            <h2 className="text-lg font-extrabold text-ink-primary">Employee management</h2>
            <p className="mt-1 text-sm text-ink-secondary">Admin and HR can manage employee records from this directory.</p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button onClick={() => setShowEmployeeForm((value) => !value)}><Plus className="h-4 w-4" /> Add Employee</Button>
            <Can roles={['admin']}>
              <Button variant="blue" onClick={() => setShowDepartmentForm((value) => !value)}><Plus className="h-4 w-4" /> Create Department</Button>
            </Can>
          </div>
        </Card>
      </Can>
      {formStatus && <p className="rounded-2xl bg-brand-blueAccent px-4 py-3 text-sm font-semibold text-brand-secondary">{formStatus}</p>}
      {showEmployeeForm && (
        <Card className="p-5">
          <h2 className="text-lg font-extrabold text-ink-primary">Add Employee</h2>
          <form className="mt-5 grid gap-4 md:grid-cols-2" onSubmit={submitEmployee}>
            {[
              ['firstName', 'First Name'],
              ['lastName', 'Last Name'],
              ['email', 'Email'],
              ['phoneNumber', 'Phone Number'],
              ['dateOfJoining', 'Date Of Joining'],
            ].map(([key, label]) => (
              <label key={key} className="block">
                <span className="text-sm font-semibold text-ink-primary">{label}</span>
                <input
                  className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none focus:border-brand-primary focus:ring-4 focus:ring-red-100"
                  type={key === 'dateOfJoining' ? 'date' : 'text'}
                  value={employeeForm[key]}
                  onChange={(event) => setEmployeeForm((current) => ({ ...current, [key]: event.target.value }))}
                  required={['firstName', 'lastName', 'email', 'dateOfJoining'].includes(key)}
                />
              </label>
            ))}
            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">Employment Type</span>
              <select className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none" value={employeeForm.employmentType} onChange={(event) => setEmployeeForm((current) => ({ ...current, employmentType: event.target.value }))}>
                <option>FULL_TIME</option>
                <option>PART_TIME</option>
                <option>CONTRACT</option>
                <option>INTERN</option>
              </select>
            </label>
            <div className="md:col-span-2">
              <Button type="submit">Create Employee</Button>
            </div>
          </form>
        </Card>
      )}
      {showDepartmentForm && (
        <Card className="p-5">
          <h2 className="text-lg font-extrabold text-ink-primary">Create Department</h2>
          <form className="mt-5 grid gap-4 md:grid-cols-2" onSubmit={submitDepartment}>
            {[
              ['companyId', 'Company ID'],
              ['name', 'Department Name'],
              ['code', 'Code'],
              ['description', 'Description'],
            ].map(([key, label]) => (
              <label key={key} className="block">
                <span className="text-sm font-semibold text-ink-primary">{label}</span>
                <input
                  className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none focus:border-brand-primary focus:ring-4 focus:ring-red-100"
                  value={departmentForm[key]}
                  onChange={(event) => setDepartmentForm((current) => ({ ...current, [key]: event.target.value }))}
                  required={key !== 'description'}
                />
              </label>
            ))}
            <div className="md:col-span-2">
              <Button type="submit" variant="blue">Create Department</Button>
            </div>
          </form>
        </Card>
      )}
      {editingEmployee && (
        <Can roles={['admin', 'hr']}>
          <Card className="p-5">
            <h2 className="text-lg font-extrabold text-ink-primary">Edit Employee Details</h2>
            <form className="mt-5 grid gap-4 md:grid-cols-2" onSubmit={submitEmployeeEdit}>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Name</span>
                <input className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none" value={editingEmployee.name} onChange={(event) => setEditingEmployee((current) => ({ ...current, name: event.target.value }))} />
              </label>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Department</span>
                <input className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none" value={editingEmployee.department} onChange={(event) => setEditingEmployee((current) => ({ ...current, department: event.target.value }))} />
              </label>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Role</span>
                <input className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none" value={editingEmployee.role} onChange={(event) => setEditingEmployee((current) => ({ ...current, role: event.target.value }))} />
              </label>
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">Status</span>
                <input className="mt-2 w-full rounded-2xl border border-line px-4 py-3 outline-none" value={editingEmployee.status} onChange={(event) => setEditingEmployee((current) => ({ ...current, status: event.target.value }))} />
              </label>
              <div className="flex gap-3 md:col-span-2">
                <Button type="submit">Save Employee</Button>
                <Button type="button" variant="secondary" onClick={() => setEditingEmployee(null)}>Cancel</Button>
              </div>
            </form>
          </Card>
        </Can>
      )}
      <section className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        {teamMembers.map((employee) => (
          <Card key={employee.id} className="p-5">
            <div className="flex items-start gap-4">
              <img src={employee.avatar} alt={employee.name} className="h-16 w-16 rounded-2xl object-cover" />
              <div className="min-w-0 flex-1">
                <h2 className="truncate text-lg font-extrabold text-ink-primary">{employee.name}</h2>
                <p className="mt-1 text-sm text-ink-secondary">{employee.role}</p>
                <p className="mt-1 text-xs font-bold uppercase text-brand-primary">{employee.department}</p>
              </div>
              <Badge>{employee.status}</Badge>
            </div>
            <div className="mt-5 flex gap-3">
              <Button variant="secondary" className="flex-1"><Mail className="h-4 w-4" /> Email</Button>
              <Button variant="secondary"><Phone className="h-4 w-4" /></Button>
              <Button variant="secondary"><MessageSquare className="h-4 w-4" /></Button>
              <Can roles={['admin', 'hr']}>
                <Button variant="secondary" onClick={() => setEditingEmployee(employee)}><Pencil className="h-4 w-4" /></Button>
              </Can>
              <Can roles={['admin']}>
                <Button variant="secondary" onClick={() => deleteEmployee(employee)}><Trash2 className="h-4 w-4" /></Button>
              </Can>
            </div>
          </Card>
        ))}
      </section>
    </div>
  );
}
