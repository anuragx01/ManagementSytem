import React, { useMemo, useState } from 'react';
import { DollarSign, Plus } from 'lucide-react';
import { Can } from '../components/RoleGate';
import { useRole } from '../context/RoleContext';
import Button from '../components/ui/Button';
import Card from '../components/ui/Card';
import PageHeader from '../components/ui/PageHeader';
import { LoadingIndicator } from '../components/ui/Skeleton';
import useApiData from '../hooks/useApiData';
import { employeesApi, pageContent, payrollApi } from '../lib/api';

const blankPayroll = {
  employeeId: '',
  payrollPeriod: '',
  basicSalary: '',
  allowances: '',
  deductions: '',
  paymentStatus: 'PENDING',
  processedDate: '',
  notes: '',
};

export default function Payroll() {
  const { activeRole } = useRole();
  const [showForm, setShowForm] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [form, setForm] = useState(blankPayroll);
  const [status, setStatus] = useState('');
  const canManage = ['admin', 'hr'].includes(activeRole.key);

  const { data: records, loading, error, refresh } = useApiData(
    async () => (canManage ? payrollApi.list({}) : payrollApi.my()),
    [],
    [canManage],
  );
  const { data: employees } = useApiData(
    async () => (canManage ? pageContent(await employeesApi.list({ size: 200 })) : []),
    [],
    [canManage],
  );

  const totalNet = useMemo(
    () => records.reduce((sum, record) => sum + Number(record.netSalary || 0), 0),
    [records],
  );

  function startCreate() {
    setEditingRecord(null);
    setForm(blankPayroll);
    setShowForm((value) => !value);
  }

  function startEdit(record) {
    setEditingRecord(record);
    setForm({
      employeeId: record.employeeId || '',
      payrollPeriod: record.payrollPeriod || '',
      basicSalary: record.basicSalary || '',
      allowances: record.allowances || '',
      deductions: record.deductions || '',
      paymentStatus: record.paymentStatus || 'PENDING',
      processedDate: record.processedDate || '',
      notes: record.notes || '',
    });
    setShowForm(true);
  }

  async function submitPayroll(event) {
    event.preventDefault();
    setStatus(editingRecord ? 'Updating payroll record...' : 'Creating payroll record...');
    const payload = {
      ...form,
      basicSalary: Number(form.basicSalary || 0),
      allowances: Number(form.allowances || 0),
      deductions: Number(form.deductions || 0),
      processedDate: form.processedDate || undefined,
    };
    try {
      if (editingRecord) {
        await payrollApi.update(editingRecord.id, payload);
      } else {
        await payrollApi.create(payload);
      }
      setStatus(editingRecord ? 'Payroll record updated.' : 'Payroll record created.');
      setShowForm(false);
      setEditingRecord(null);
      setForm(blankPayroll);
      refresh();
    } catch (err) {
      setStatus(err.message);
    }
  }

  return (
    <div className="page-stack">
      <PageHeader
        kicker="Payroll"
        title={canManage ? 'Payroll operations' : 'My payroll'}
        description={canManage ? 'Manage employee salary records and payment status.' : 'View your salary and payment status.'}
      />
      {error && <p className="alert-warning" role="alert">{error}</p>}
      {status && <p className="alert-info" role="status">{status}</p>}
      {loading && <LoadingIndicator message="Loading payroll records..." />}

      <section className="card-grid sm:grid-cols-3">
        <Card className="p-5" interactive={false}>
          <p className="text-sm text-ink-secondary">Records</p>
          <p className="mt-2 text-3xl font-extrabold">{records.length}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm text-ink-secondary">Net Salary</p>
          <p className="mt-2 text-3xl font-extrabold">{totalNet.toLocaleString()}</p>
        </Card>
        <Card className="p-5" interactive={false}>
          <p className="text-sm text-ink-secondary">Paid</p>
          <p className="mt-2 text-3xl font-extrabold">{records.filter((record) => record.paymentStatus === 'PAID').length}</p>
        </Card>
      </section>

      <Can roles={['admin', 'hr']}>
        <Card className="flex flex-col justify-between gap-4 p-5 sm:flex-row sm:items-center" interactive={false}>
          <div>
            <h2 className="section-title">Payroll management</h2>
            <p className="mt-1 text-sm text-ink-secondary">Create and update payroll records for employees.</p>
          </div>
          <Button onClick={startCreate}><Plus className="h-4 w-4" /> Create Payroll</Button>
        </Card>
      </Can>

      {showForm && canManage && (
        <Card className="p-5" interactive={false}>
          <h2 className="section-title">{editingRecord ? 'Update Payroll' : 'Create Payroll'}</h2>
          <form className="mt-5 grid gap-4 md:grid-cols-2" onSubmit={submitPayroll}>
            <label className="block md:col-span-2">
              <span className="text-sm font-semibold text-ink-primary">Employee</span>
              <select className="select-control mt-2" value={form.employeeId} onChange={(event) => setForm((current) => ({ ...current, employeeId: event.target.value }))} required>
                <option value="">Select employee</option>
                {employees.map((employee) => (
                  <option key={employee.id} value={employee.id}>
                    {[employee.firstName, employee.lastName].filter(Boolean).join(' ')} - {employee.employeeId}
                  </option>
                ))}
              </select>
            </label>
            {[
              ['payrollPeriod', 'Payroll Period'],
              ['basicSalary', 'Basic Salary'],
              ['allowances', 'Allowances'],
              ['deductions', 'Deductions'],
              ['processedDate', 'Processed Date'],
            ].map(([key, label]) => (
              <label key={key} className="block">
                <span className="text-sm font-semibold text-ink-primary">{label}</span>
                <input
                  className="field-control mt-2"
                  type={key === 'processedDate' ? 'date' : key === 'payrollPeriod' ? 'text' : 'number'}
                  placeholder={key === 'payrollPeriod' ? '2026-07' : undefined}
                  value={form[key]}
                  onChange={(event) => setForm((current) => ({ ...current, [key]: event.target.value }))}
                  required={['payrollPeriod', 'basicSalary'].includes(key)}
                />
              </label>
            ))}
            <label className="block">
              <span className="text-sm font-semibold text-ink-primary">Payment Status</span>
              <select className="select-control mt-2" value={form.paymentStatus} onChange={(event) => setForm((current) => ({ ...current, paymentStatus: event.target.value }))}>
                <option>PENDING</option>
                <option>PROCESSING</option>
                <option>PAID</option>
                <option>FAILED</option>
                <option>HOLD</option>
              </select>
            </label>
            <label className="block md:col-span-2">
              <span className="text-sm font-semibold text-ink-primary">Notes</span>
              <input className="field-control mt-2" value={form.notes} onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))} />
            </label>
            <div className="flex gap-3 md:col-span-2">
              <Button type="submit">{editingRecord ? 'Update Payroll' : 'Create Payroll'}</Button>
              <Button type="button" variant="secondary" onClick={() => setShowForm(false)}>Cancel</Button>
            </div>
          </form>
        </Card>
      )}

      <Card className="overflow-hidden" interactive={false}>
        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-surface-muted text-xs uppercase text-ink-secondary">
              <tr>{['Employee', 'Employee ID', 'Department', 'Period', 'Basic', 'Allowances', 'Deductions', 'Net', 'Status', 'Processed', ''].map((head) => <th key={head} className="px-5 py-4">{head}</th>)}</tr>
            </thead>
            <tbody className="divide-y divide-line">
              {records.length === 0 && (
                <tr><td colSpan={11} className="px-5 py-8 text-center text-ink-secondary">No payroll records yet.</td></tr>
              )}
              {records.map((record) => (
                <tr key={record.id}>
                  <td className="px-5 py-4 font-bold text-ink-primary">{record.employeeName || '-'}</td>
                  <td className="px-5 py-4 text-ink-secondary">{record.employeeCode || '-'}</td>
                  <td className="px-5 py-4 text-ink-secondary">{record.departmentName || '-'}</td>
                  <td className="px-5 py-4 text-ink-secondary">{record.payrollPeriod}</td>
                  <td className="px-5 py-4 text-ink-secondary">{record.basicSalary}</td>
                  <td className="px-5 py-4 text-ink-secondary">{record.allowances}</td>
                  <td className="px-5 py-4 text-ink-secondary">{record.deductions}</td>
                  <td className="px-5 py-4 font-bold text-ink-primary">{record.netSalary}</td>
                  <td className="px-5 py-4 text-ink-secondary">{record.paymentStatus}</td>
                  <td className="px-5 py-4 text-ink-secondary">{record.processedDate || '-'}</td>
                  <td className="px-5 py-4">
                    {canManage && <Button variant="secondary" onClick={() => startEdit(record)}>Edit</Button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}

