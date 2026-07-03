CREATE TABLE daily_work_reports (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id     UUID NOT NULL REFERENCES employees(id),
    report_date     DATE NOT NULL,
    completed_work  TEXT NOT NULL,
    pending_work    TEXT,
    tomorrow_plan   TEXT,
    blockers        TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_daily_report_employee_date UNIQUE (employee_id, report_date)
);

CREATE INDEX idx_daily_reports_employee ON daily_work_reports(employee_id);
CREATE INDEX idx_daily_reports_date ON daily_work_reports(report_date);
