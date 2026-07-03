CREATE TABLE payroll_records (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id          UUID NOT NULL REFERENCES employees(id),
    payroll_period       VARCHAR(20) NOT NULL,
    basic_salary         NUMERIC(12, 2) NOT NULL DEFAULT 0,
    allowances           NUMERIC(12, 2) NOT NULL DEFAULT 0,
    deductions           NUMERIC(12, 2) NOT NULL DEFAULT 0,
    net_salary           NUMERIC(12, 2) NOT NULL DEFAULT 0,
    payment_status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_date       DATE,
    notes                TEXT,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP,
    created_by           VARCHAR(255),
    updated_by           VARCHAR(255),
    is_deleted           BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_payroll_employee_id ON payroll_records(employee_id);
CREATE INDEX idx_payroll_period ON payroll_records(payroll_period);
CREATE INDEX idx_payroll_status ON payroll_records(payment_status);

CREATE TABLE announcements (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title               VARCHAR(160) NOT NULL,
    message             TEXT NOT NULL,
    audience_role       VARCHAR(40),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    published_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_announcements_active ON announcements(active);
CREATE INDEX idx_announcements_audience_role ON announcements(audience_role);
