-- time_entries
CREATE TABLE time_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id),
    task_id UUID REFERENCES tasks(id),
    description VARCHAR(500),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_minutes INT,
    billable BOOLEAN NOT NULL DEFAULT TRUE,
    timer_status VARCHAR(10) NOT NULL DEFAULT 'STOPPED'
        CHECK (timer_status IN ('RUNNING','PAUSED','STOPPED')),
    paused_at TIMESTAMP,
    total_paused_minutes INT NOT NULL DEFAULT 0,
    date DATE NOT NULL,
    timesheet_id UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX idx_te_employee_date ON time_entries(employee_id, date);
CREATE INDEX idx_te_project ON time_entries(project_id);
CREATE INDEX idx_te_task ON time_entries(task_id);

-- timesheets
CREATE TABLE timesheets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    total_minutes INT NOT NULL DEFAULT 0,
    billable_minutes INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','REJECTED')),
    approver_id UUID REFERENCES employees(id),
    approved_at TIMESTAMP,
    approver_remarks VARCHAR(500),
    submitted_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE(employee_id, week_start_date)
);
