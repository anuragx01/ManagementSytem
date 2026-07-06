import React, { useMemo, useState } from 'react';
import { useRole } from '../context/RoleContext';
import PageHeader from '../components/ui/PageHeader';
import PageTabs from '../components/ui/PageTabs';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import { LoadingIndicator } from '../components/ui/Skeleton';
import { pageContent } from '../services/baseApi';
import { useGetCompanyQuery } from '../services/organizationApi';
import { useApplyLeaveMutation, useApproveHrLeaveMutation, useApproveManagerLeaveMutation, useGetLeaveTypesQuery, useGetMyLeaveBalanceQuery, useGetMyLeavesQuery, useGetPendingHrLeavesQuery, useGetPendingManagerLeavesQuery } from '../services/leaveApi';

const employeeTabs = [
  { id: 'my-leave', label: 'My Leave' },
];

const hrTabs = [
  { id: 'my-leave', label: 'My Leave' },
  { id: 'approval', label: 'Leave Approval' },
];

const adminTabs = [
  { id: 'my-leave', label: 'My Leave' },
  { id: 'all-leaves', label: 'All Employee Leaves' },
  { id: 'analytics', label: 'Leave Analytics' },
];

const managerTabs = [
  { id: 'my-leave', label: 'My Leave' },
  { id: 'team-leave', label: 'Team Leave' },
];

export default function Leave() {
  const { activeRole } = useRole();
  const tabs = useMemo(() => {
    if (activeRole.key === 'admin') return adminTabs;
    if (activeRole.key === 'hr') return hrTabs;
    if (activeRole.key === 'manager') return managerTabs;
    return employeeTabs;
  }, [activeRole.key]);

  const [activeTab, setActiveTab] = useState(tabs[0].id);
  const [applyStatus, setApplyStatus] = useState('');
  const [applyForm, setApplyForm] = useState({
    leaveTypeId: '',
    startDate: '',
    endDate: '',
    dayType: 'FULL',
    reason: '',
  });
  const { data: company } = useGetCompanyQuery();
  const { data: myLeaves, isLoading: myLoading, error: myError, refetch: refreshMyLeaves } = useGetMyLeavesQuery({ page: 0, size: 20 });
  const { data: pendingHr = [], isLoading: hrLoading } = useGetPendingHrLeavesQuery(undefined, { skip: !(activeRole.key === 'hr' || activeRole.key === 'admin') });
  const { data: pendingManager = [], isLoading: managerLoading } = useGetPendingManagerLeavesQuery(undefined, { skip: activeRole.key !== 'manager' });
  const { data: balances = [] } = useGetMyLeaveBalanceQuery();
  const { data: leaveTypes = [] } = useGetLeaveTypesQuery(company?.id, { skip: !company?.id });
  const [applyLeave] = useApplyLeaveMutation();
  const [approveHrLeave] = useApproveHrLeaveMutation();
  const [approveManagerLeave] = useApproveManagerLeaveMutation();

  async function submitLeaveApplication(event) {
    event.preventDefault();
    setApplyStatus('Submitting leave request...');
    try {
      await applyLeave(applyForm).unwrap();
      setApplyStatus('Leave request submitted successfully.');
      refreshMyLeaves();
      setApplyForm({ leaveTypeId: '', startDate: '', endDate: '', dayType: 'FULL', reason: '' });
    } catch (err) {
      setApplyStatus(err.message || 'Request failed.');
    }
  }

  const rows = activeTab === 'my-leave'
    ? pageContent(myLeaves)
    : activeTab === 'approval'
      ? pendingHr || []
      : activeTab === 'team-leave'
        ? pendingManager || []
        : activeTab === 'all-leaves'
          ? pendingHr || []
          : [];

  return (
    <div className="page-stack">
      <PageHeader
        kicker="Leave Management"
        title={activeRole.key === 'employee' ? 'My Leave' : 'Leave workspace'}
        description="Leave requests, balances, and approvals mapped to /leaves APIs."
      />

      <Card className="overflow-hidden" interactive={false}>
        <div className="px-5 pt-5">
          <PageTabs tabs={tabs} activeTab={activeTab} onChange={setActiveTab} />
        </div>

        <div className="space-y-6 p-5">
          {(myLoading || hrLoading || managerLoading) && <LoadingIndicator message="Loading leave data..." />}
          {myError && <p className="alert-warning" role="alert">{myError.message || "Unable to load leave data."}</p>}

          {activeTab === 'my-leave' && (
            <>
              <Card className="p-5" interactive={false}>
                <h2 className="section-title">Apply for leave</h2>
                <form className="mt-4 grid gap-4 md:grid-cols-2" onSubmit={submitLeaveApplication}>
                  <label className="block md:col-span-2">
                    <span className="text-sm font-semibold text-ink-primary">Leave Type</span>
                    <select
                      className="select-control mt-2"
                      value={applyForm.leaveTypeId}
                      onChange={(event) => setApplyForm((current) => ({ ...current, leaveTypeId: event.target.value }))}
                      required
                    >
                      <option value="">Select leave type</option>
                      {(leaveTypes || []).map((type) => (
                        <option key={type.id} value={type.id}>{type.name}</option>
                      ))}
                    </select>
                  </label>
                  <label className="block">
                    <span className="text-sm font-semibold text-ink-primary">Start Date</span>
                    <input type="date" className="field-control mt-2" value={applyForm.startDate} onChange={(event) => setApplyForm((current) => ({ ...current, startDate: event.target.value }))} required />
                  </label>
                  <label className="block">
                    <span className="text-sm font-semibold text-ink-primary">End Date</span>
                    <input type="date" className="field-control mt-2" value={applyForm.endDate} onChange={(event) => setApplyForm((current) => ({ ...current, endDate: event.target.value }))} required />
                  </label>
                  <label className="block md:col-span-2">
                    <span className="text-sm font-semibold text-ink-primary">Reason</span>
                    <textarea className="field-control mt-2 min-h-24" value={applyForm.reason} onChange={(event) => setApplyForm((current) => ({ ...current, reason: event.target.value }))} required />
                  </label>
                  <div className="md:col-span-2">
                    <Button type="submit">Submit Request</Button>
                  </div>
                </form>
                {applyStatus && <p className="mt-4 text-sm text-ink-secondary">{applyStatus}</p>}
              </Card>
              <section className="card-grid sm:grid-cols-2 lg:grid-cols-3">
              {(balances || []).map((balance) => (
                <div key={balance.leaveTypeId || balance.leaveTypeName} className="rounded-2xl border border-line p-4">
                  <p className="text-xs font-bold uppercase text-ink-secondary">{balance.leaveTypeName || 'Leave Type'}</p>
                  <p className="mt-2 text-2xl font-extrabold text-ink-primary">{balance.availableDays ?? balance.remainingDays ?? '—'}</p>
                </div>
              ))}
              </section>
            </>
          )}

          <div className="overflow-x-auto rounded-2xl border border-line">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="sticky top-0 bg-surface-muted text-xs uppercase text-ink-secondary">
                <tr>
                  {['Employee', 'From', 'To', 'Status', 'Actions'].map((head) => (
                    <th key={head} className="px-5 py-4 font-bold">{head}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {rows.length === 0 && (
                  <tr>
                    <td colSpan={5} className="px-5 py-8 text-center text-ink-secondary">No leave records for this view.</td>
                  </tr>
                )}
                {rows.map((leave) => (
                  <tr key={leave.id} className="border-t border-line">
                    <td className="px-5 py-4 font-semibold text-ink-primary">{leave.employeeName || 'Me'}</td>
                    <td className="px-5 py-4 text-ink-secondary">{leave.fromDate || leave.startDate}</td>
                    <td className="px-5 py-4 text-ink-secondary">{leave.toDate || leave.endDate}</td>
                    <td className="px-5 py-4 capitalize text-ink-secondary">{leave.status?.toLowerCase?.() || leave.status}</td>
                    <td className="px-5 py-4">
                      {(activeTab === 'approval' || activeTab === 'team-leave') && (
                        <div className="flex gap-2">
                          <Button
                            onClick={async () => {
                              try {
                                await (activeTab === 'team-leave' ? approveManagerLeave : approveHrLeave)({ leaveRequestId: leave.id, decision: 'APPROVED' }).unwrap();
                                setApplyStatus('Leave request approved.');
                              } catch (err) {
                                setApplyStatus(err.message || 'Approval failed.');
                              }
                            }}
                          >
                            Approve
                          </Button>
                          <Button
                            variant="secondary"
                            onClick={async () => {
                              try {
                                await (activeTab === 'team-leave' ? approveManagerLeave : approveHrLeave)({ leaveRequestId: leave.id, decision: 'REJECTED' }).unwrap();
                                setApplyStatus('Leave request rejected.');
                              } catch (err) {
                                setApplyStatus(err.message || 'Rejection failed.');
                              }
                            }}
                          >
                            Reject
                          </Button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </Card>
    </div>
  );
}






