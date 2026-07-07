import React, { useMemo, useState } from 'react';
import { Mail, Pencil, Phone, Plus, Trash2 } from 'lucide-react';
import { Can } from '../components/RoleGate';
import Badge from '../components/ui/Badge';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import SearchFilter from '../components/ui/SearchFilter';
import { LoadingIndicator } from '../components/ui/Skeleton';
import { pageContent } from '../services/baseApi';
import {
  useCreateEmployeeMutation,
  useGetEmployeesQuery,
  useTerminateEmployeeMutation,
  useUpdateEmployeeMutation,
} from '../services/employeeApi';
import {
  useCreateDepartmentMutation,
  useGetCompanyQuery,
  useGetDepartmentsQuery,
} from '../services/organizationApi';
import { mapEmployee } from '../lib/mappers';

const emptyEmployeeForm = {
  firstName: '',
  lastName: '',
  employeeId: '',
  email: '',
  phoneNumber: '',
  role: 'EMPLOYEE',
  departmentId: '',
  dateOfJoining: '',
  employmentType: 'FULL_TIME',
  workLocation: 'HYBRID',
};

function validateEmployeeForm(form) {
  const errors = [];

  const nameRegex = /^[A-Za-z]+(?:[ '-][A-Za-z]+)*$/;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
  const phoneRegex = /^[6-9]\d{9}$/;
  const employeeIdRegex = /^[A-Za-z0-9_-]{2,30}$/;

  if (!form.firstName.trim()) {
    errors.push('First name is required.');
  } else if (!nameRegex.test(form.firstName.trim())) {
    errors.push('First name must contain only letters.');
  }

  if (!form.lastName.trim()) {
    errors.push('Last name is required.');
  } else if (!nameRegex.test(form.lastName.trim())) {
    errors.push('Last name must contain only letters.');
  }

  if (!form.employeeId.trim()) {
    errors.push('Employee ID is required.');
  } else if (!employeeIdRegex.test(form.employeeId.trim())) {
    errors.push(
      'Employee ID can contain only letters, numbers, hyphens and underscores.'
    );
  }

  if (!form.email.trim()) {
    errors.push('Email is required.');
  } else if (!emailRegex.test(form.email.trim())) {
    errors.push('Enter a valid email address.');
  }

  if (!form.phoneNumber.trim()) {
    errors.push('Phone number is required.');
  } else if (!phoneRegex.test(form.phoneNumber.trim())) {
    errors.push('Enter a valid 10-digit Indian phone number.');
  }

  if (!form.departmentId) {
    errors.push('Department is required.');
  }

  if (!form.dateOfJoining) {
    errors.push('Date of joining is required.');
  } else {
    const joiningDate = new Date(`${form.dateOfJoining}T00:00:00`);
    const today = new Date();

    today.setHours(0, 0, 0, 0);

    if (Number.isNaN(joiningDate.getTime())) {
      errors.push('Enter a valid date of joining.');
    } else if (joiningDate > today) {
      errors.push('Date of joining cannot be in the future.');
    }
  }

  if (!form.role) {
    errors.push('Role is required.');
  }

  if (!form.employmentType) {
    errors.push('Employment type is required.');
  }

  if (!form.workLocation) {
    errors.push('Work location is required.');
  }

  return errors;
}

export default function TeamDirectory() {
  const [searchQuery, setSearchQuery] = useState('');
  const [departmentFilter, setDepartmentFilter] = useState('');
  const [showDepartmentForm, setShowDepartmentForm] = useState(false);
  const [showEmployeeForm, setShowEmployeeForm] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const [formStatus, setFormStatus] = useState('');
  const [formErrors, setFormErrors] = useState([]);
  const [employeeForm, setEmployeeForm] = useState(emptyEmployeeForm);

  const [departmentForm, setDepartmentForm] = useState({
    companyId: '',
    name: '',
    code: '',
    description: '',
    active: true,
  });

  const { data: company } = useGetCompanyQuery();

  const {
    data: employeePage,
    isLoading: loading,
    error,
    refetch,
  } = useGetEmployeesQuery({ size: 50 });

  const { data: departments = [] } = useGetDepartmentsQuery(
    { companyId: company?.id },
    { skip: !company?.id }
  );

  const [createEmployee] = useCreateEmployeeMutation();
  const [updateEmployee] = useUpdateEmployeeMutation();
  const [terminateEmployee] = useTerminateEmployeeMutation();
  const [createDepartment] = useCreateDepartmentMutation();

  const teamMembers = useMemo(
    () => pageContent(employeePage).map(mapEmployee),
    [employeePage]
  );

  const departmentOptions = useMemo(
    () =>
      (
        departments.length > 0
          ? departments.map((department) => department.name)
          : [
              ...new Set(
                teamMembers
                  .map((member) => member.department)
                  .filter(Boolean)
              ),
            ]
      ).sort(),
    [departments, teamMembers]
  );

  const filteredMembers = useMemo(() => {
    const term = searchQuery.trim().toLowerCase();

    return teamMembers.filter((member) => {
      const matchesSearch =
        !term ||
        [
          member.name,
          member.email,
          member.raw?.employeeId,
          member.department,
          member.role,
        ].some((value) =>
          String(value || '')
            .toLowerCase()
            .includes(term)
        );

      const matchesDepartment =
        !departmentFilter || member.department === departmentFilter;

      return matchesSearch && matchesDepartment;
    });
  }, [teamMembers, searchQuery, departmentFilter]);

  async function submitEmployee(event) {
    event.preventDefault();

    const validationErrors = validateEmployeeForm(employeeForm);

    setFormErrors(validationErrors);

    if (validationErrors.length) return;

    setFormStatus('Creating employee...');

    try {
      const { role, ...employeePayload } = employeeForm;

      await createEmployee(employeePayload).unwrap();

      setFormStatus(
        'Employee created successfully. Role assignment requires backend verification.'
      );

      setEmployeeForm(emptyEmployeeForm);
      setFormErrors([]);
      setShowEmployeeForm(false);
      refetch();
    } catch (err) {
      setFormStatus(
        `Employee create failed: ${err.message || 'Request failed.'}`
      );
    }
  }

  async function submitDepartment(event) {
    event.preventDefault();

    setFormStatus('Creating department...');

    try {
      await createDepartment({
        ...departmentForm,
        companyId: company?.id || departmentForm.companyId,
      }).unwrap();

      setFormStatus('Department created successfully.');
      setShowDepartmentForm(false);
    } catch (err) {
      setFormStatus(
        `Department create failed: ${err.message || 'Request failed.'}`
      );
    }
  }

  async function submitEmployeeEdit(event) {
    event.preventDefault();

    setFormStatus('Updating employee...');

    try {
      const raw = editingEmployee.raw || {};

      const [firstName = '', ...rest] = String(
        editingEmployee.name || ''
      )
        .trim()
        .split(' ');

      const selectedDepartment = departments.find(
        (department) => department.name === editingEmployee.department
      );

      await updateEmployee({
        id: editingEmployee.id,
        body: {
          ...raw,
          firstName: firstName || raw.firstName,
          lastName: rest.join(' ') || raw.lastName,
          departmentId: selectedDepartment?.id || raw.departmentId,
          status:
            editingEmployee.status === 'Available'
              ? 'ACTIVE'
              : editingEmployee.status?.toUpperCase?.() || raw.status,
        },
      }).unwrap();

      setFormStatus('Employee updated successfully.');
      setEditingEmployee(null);
    } catch (err) {
      setFormStatus(
        `Employee update failed: ${err.message || 'Request failed.'}`
      );
    }
  }

  async function deleteEmployee(employee) {
    setFormStatus('Deleting employee...');

    try {
      await terminateEmployee({
        id: employee.id,
        reason: 'Deleted from admin UI',
      }).unwrap();

      setFormStatus('Employee deleted/terminated successfully.');
    } catch (err) {
      setFormStatus(
        `Employee delete failed: ${err.message || 'Request failed.'}`
      );
    }
  }

  return (
    <div className="page-stack">
      <div className="page-header">
        <p className="page-kicker">Team Directory</p>
        <h1 className="page-title">People across NEXSTAR</h1>
      </div>

      <SearchFilter
        placeholder="Search by name, email, or employee ID"
        filterLabel="All Departments"
        options={departmentOptions}
        searchValue={searchQuery}
        onSearchChange={setSearchQuery}
        filterValue={departmentFilter}
        onFilterChange={setDepartmentFilter}
      />

      {error && (
        <p className="alert-warning" role="alert">
          Unable to load employees: {error.message || 'Request failed.'}
        </p>
      )}

      {loading && (
        <LoadingIndicator message="Loading employees from backend..." />
      )}

      <Can roles={['admin', 'hr']}>
        <Card
          className="flex flex-col justify-between gap-4 p-5 sm:flex-row sm:items-center"
          interactive={false}
        >
          <div>
            <h2 className="section-title">Employee management</h2>
            <p className="mt-1 text-sm text-ink-secondary">
              Admin and HR can manage employee records from this directory.
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <Button
              onClick={() => {
                setShowEmployeeForm((value) => !value);
                setFormErrors([]);
                setFormStatus('');
              }}
            >
              <Plus className="h-4 w-4" />
              Add Employee
            </Button>

            <Can roles={['admin']}>
              <Button
                variant="blue"
                onClick={() =>
                  setShowDepartmentForm((value) => !value)
                }
              >
                <Plus className="h-4 w-4" />
                Create Department
              </Button>
            </Can>
          </div>
        </Card>
      </Can>

      {formStatus && (
        <p className="alert-info" role="status">
          {formStatus}
        </p>
      )}

      {formErrors.length > 0 && (
        <div className="alert-warning" role="alert">
          {formErrors.map((item) => (
            <p key={item}>{item}</p>
          ))}
        </div>
      )}

      {showEmployeeForm && (
        <Card className="p-5" interactive={false}>
          <h2 className="section-title">Add Employee</h2>

          <form
            className="mt-5 grid gap-4 md:grid-cols-2"
            onSubmit={submitEmployee}
            noValidate
          >
            <label className="block md:col-span-2">
              <span className="text-sm font-semibold text-ink-primary">
                Employee Name
              </span>

              <div className="mt-2 grid gap-3 sm:grid-cols-2">
                <input
                  className="field-control"
                  type="text"
                  placeholder="First name"
                  value={employeeForm.firstName}
                  maxLength={50}
                  onChange={(event) => {
                    const value = event.target.value;

                    if (/^[A-Za-z '-]*$/.test(value)) {
                      setEmployeeForm((current) => ({
                        ...current,
                        firstName: value,
                      }));
                    }
                  }}
                  required
                />

                <input
                  className="field-control"
                  type="text"
                  placeholder="Last name"
                  value={employeeForm.lastName}
                  maxLength={50}
                  onChange={(event) => {
                    const value = event.target.value;

                    if (/^[A-Za-z '-]*$/.test(value)) {
                      setEmployeeForm((current) => ({
                        ...current,
                        lastName: value,
                      }));
                    }
                  }}
                  required
                />
              </div>
            </label>

            {[
              ['employeeId', 'Employee ID'],
              ['email', 'Email'],
              ['phoneNumber', 'Phone'],
              ['dateOfJoining', 'Date Of Joining'],
            ].map(([key, label]) => (
              <label key={key} className="block">
                <span className="text-sm font-semibold text-ink-primary">
                  {label}
                </span>

                <input
                  className="field-control mt-2"
                  type={
                    key === 'dateOfJoining'
                      ? 'date'
                      : key === 'email'
                        ? 'email'
                        : key === 'phoneNumber'
                          ? 'tel'
                          : 'text'
                  }
                  value={employeeForm[key]}
                  max={
                    key === 'dateOfJoining'
                      ? new Date().toISOString().split('T')[0]
                      : undefined
                  }
                  maxLength={
                    key === 'phoneNumber'
                      ? 10
                      : key === 'employeeId'
                        ? 30
                        : undefined
                  }
                  inputMode={
                    key === 'phoneNumber' ? 'numeric' : undefined
                  }
                  autoComplete={
                    key === 'email'
                      ? 'email'
                      : key === 'phoneNumber'
                        ? 'tel'
                        : undefined
                  }
                  onChange={(event) => {
                    let value = event.target.value;

                    if (key === 'phoneNumber') {
                      value = value.replace(/\D/g, '').slice(0, 10);
                    }

                    setEmployeeForm((current) => ({
                      ...current,
                      [key]: value,
                    }));
                  }}
                  required
                />
              </label>
            ))}

            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">
                Role
              </span>

              <select
                className="select-control mt-2"
                value={employeeForm.role}
                onChange={(event) =>
                  setEmployeeForm((current) => ({
                    ...current,
                    role: event.target.value,
                  }))
                }
              >
                <option value="EMPLOYEE">EMPLOYEE</option>
                <option value="HR">HR</option>
                <option value="MANAGER">MANAGER</option>
                <option value="TEAM_LEAD">TEAM_LEAD</option>
              </select>
            </label>

            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">
                Department
              </span>

              <select
                className="select-control mt-2"
                value={employeeForm.departmentId}
                onChange={(event) =>
                  setEmployeeForm((current) => ({
                    ...current,
                    departmentId: event.target.value,
                  }))
                }
                required
              >
                <option value="">Select department</option>

                {departments.map((department) => (
                  <option key={department.id} value={department.id}>
                    {department.name}
                  </option>
                ))}
              </select>
            </label>

            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">
                Employment Type
              </span>

              <select
                className="select-control mt-2"
                value={employeeForm.employmentType}
                onChange={(event) =>
                  setEmployeeForm((current) => ({
                    ...current,
                    employmentType: event.target.value,
                  }))
                }
                required
              >
                <option value="FULL_TIME">FULL_TIME</option>
                <option value="PART_TIME">PART_TIME</option>
                <option value="CONTRACT">CONTRACT</option>
                <option value="INTERN">INTERN</option>
              </select>
            </label>

            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">
                Work Location
              </span>

              <select
                className="select-control mt-2"
                value={employeeForm.workLocation}
                onChange={(event) =>
                  setEmployeeForm((current) => ({
                    ...current,
                    workLocation: event.target.value,
                  }))
                }
                required
              >
                <option value="HYBRID">HYBRID</option>
                <option value="REMOTE">REMOTE</option>
                <option value="OFFICE">OFFICE</option>
              </select>
            </label>

            <div className="md:col-span-2">
              <Button type="submit">Create Employee</Button>
            </div>
          </form>
        </Card>
      )}

      {showDepartmentForm && (
        <Card className="p-5" interactive={false}>
          <h2 className="section-title">Create Department</h2>

          <form
            className="mt-5 grid gap-4 md:grid-cols-2"
            onSubmit={submitDepartment}
          >
            {[
              ['name', 'Department Name'],
              ['code', 'Code'],
              ['description', 'Description'],
            ].map(([key, label]) => (
              <label key={key} className="block">
                <span className="text-sm font-semibold text-ink-primary">
                  {label}
                </span>

                <input
                  className="field-control mt-2"
                  value={departmentForm[key]}
                  onChange={(event) =>
                    setDepartmentForm((current) => ({
                      ...current,
                      [key]: event.target.value,
                    }))
                  }
                  required={key !== 'description'}
                />
              </label>
            ))}

            <div className="md:col-span-2">
              <Button type="submit" variant="blue">
                Create Department
              </Button>
            </div>
          </form>
        </Card>
      )}

      {editingEmployee && (
        <Can roles={['admin', 'hr']}>
          <Card className="p-5" interactive={false}>
            <h2 className="section-title">Edit Employee Details</h2>

            <form
              className="mt-5 grid gap-4 md:grid-cols-2"
              onSubmit={submitEmployeeEdit}
            >
              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">
                  Name
                </span>

                <input
                  className="field-control mt-2"
                  value={editingEmployee.name}
                  onChange={(event) =>
                    setEditingEmployee((current) => ({
                      ...current,
                      name: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">
                  Department
                </span>

                <select
                  className="select-control mt-2"
                  value={
                    editingEmployee.raw?.departmentId ||
                    departments.find(
                      (department) =>
                        department.name === editingEmployee.department
                    )?.id ||
                    ''
                  }
                  onChange={(event) => {
                    const department = departments.find(
                      (item) => String(item.id) === event.target.value
                    );

                    setEditingEmployee((current) => ({
                      ...current,
                      department:
                        department?.name || current.department,
                      raw: {
                        ...current.raw,
                        departmentId: event.target.value,
                      },
                    }));
                  }}
                >
                  <option value="">Select department</option>

                  {departments.map((department) => (
                    <option key={department.id} value={department.id}>
                      {department.name}
                    </option>
                  ))}
                </select>
              </label>

              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">
                  Role
                </span>

                <input
                  className="field-control mt-2"
                  value={editingEmployee.role}
                  onChange={(event) =>
                    setEditingEmployee((current) => ({
                      ...current,
                      role: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="block">
                <span className="text-sm font-semibold text-ink-primary">
                  Status
                </span>

                <input
                  className="field-control mt-2"
                  value={editingEmployee.status}
                  onChange={(event) =>
                    setEditingEmployee((current) => ({
                      ...current,
                      status: event.target.value,
                    }))
                  }
                />
              </label>

              <div className="flex gap-3 md:col-span-2">
                <Button type="submit">Save Employee</Button>

                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => setEditingEmployee(null)}
                >
                  Cancel
                </Button>
              </div>
            </form>
          </Card>
        </Can>
      )}

      <section className="card-grid md:grid-cols-2 xl:grid-cols-3">
        {filteredMembers.length === 0 && !loading && (
          <Card
            className="p-5 md:col-span-2 xl:col-span-3"
            interactive={false}
          >
            <p className="text-sm text-ink-secondary">
              No team members match your search.
            </p>
          </Card>
        )}

        {filteredMembers.map((employee) => (
          <Card
            key={employee.id}
            className="p-5 transition duration-200 hover:-translate-y-0.5 hover:shadow-soft"
          >
            <div className="flex items-start gap-4">
              <img
                src={employee.avatar}
                alt={employee.name}
                className="h-16 w-16 rounded-2xl object-cover"
              />

              <div className="min-w-0 flex-1">
                <p className="text-xs font-bold uppercase text-ink-secondary">
                  {employee.raw?.employeeId || 'Employee'}
                </p>

                <h2 className="section-title truncate">
                  {employee.name}
                </h2>

                <p className="mt-1 text-sm text-ink-secondary">
                  Designation: {employee.role}
                </p>

                <p className="mt-1 text-xs font-bold uppercase text-brand-primary">
                  Department: {employee.department}
                </p>
              </div>

              <Badge>{employee.status}</Badge>
            </div>

            <div className="mt-4 space-y-2 text-sm text-ink-secondary">
              <p className="flex items-center gap-2">
                <Mail className="h-4 w-4 shrink-0" />
                {employee.email || '-'}
              </p>

              <p className="flex items-center gap-2">
                <Phone className="h-4 w-4 shrink-0" />
                {employee.phone || employee.raw?.phoneNumber || '-'}
              </p>
            </div>

            <div className="mt-5 flex flex-wrap gap-3">
              {employee.email && (
                <Button
                  variant="secondary"
                  className="flex-1"
                  onClick={() => {
                    window.location.href = `mailto:${employee.email}`;
                  }}
                >
                  <Mail className="h-4 w-4" />
                  Email
                </Button>
              )}

              <Can roles={['admin', 'hr']}>
                <Button
                  variant="secondary"
                  onClick={() => setEditingEmployee(employee)}
                  aria-label={`Edit ${employee.name}`}
                >
                  <Pencil className="h-4 w-4" />
                </Button>
              </Can>

              <Can roles={['admin']}>
                <Button
                  variant="secondary"
                  onClick={() => deleteEmployee(employee)}
                  aria-label={`Delete ${employee.name}`}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </Can>
            </div>
          </Card>
        ))}
      </section>
    </div>
  );
}