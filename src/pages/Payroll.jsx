import React from 'react';
import { DollarSign } from 'lucide-react';
import Card from '../components/ui/Card';
import PageHeader from '../components/ui/PageHeader';

export default function Payroll() {
  return (
    <div className="page-stack">
      <PageHeader
        kicker="Payroll"
        title="Payroll operations"
        description="Payroll navigation is available for Admin, but payroll data is not shown until a backend payroll API is connected."
      />
      <Card className="p-6" interactive={false}>
        <div className="flex items-start gap-4">
          <div className="grid h-12 w-12 shrink-0 place-items-center rounded-2xl bg-brand-blueAccent text-brand-secondary">
            <DollarSign className="h-5 w-5" />
          </div>
          <div>
            <h2 className="section-title">Payroll API not connected</h2>
            <p className="mt-2 max-w-3xl text-sm leading-6 text-ink-secondary">
              This page intentionally avoids fake payroll summaries. Connect payroll endpoints for salary runs,
              payslips, deductions, reimbursements, and statutory reports, then this module can render live operational data.
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
}
