-- ============================================================
-- V6: Leave Management Tables
-- ============================================================

-- Leave Types
CREATE TABLE leave_types (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id              UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name                    VARCHAR(100) NOT NULL,
    code                    VARCHAR(10),
    description             TEXT,
    max_days_per_year       INT NOT NULL DEFAULT 0,
    max_consecutive_days    INT,
    is_paid                 BOOLEAN NOT NULL DEFAULT TRUE,
    carry_forward_allowed   BOOLEAN NOT NULL DEFAULT FALSE,
    max_carry_forward_days  INT,
    half_day_allowed        BOOLEAN NOT NULL DEFAULT TRUE,
    document_required       BOOLEAN NOT NULL DEFAULT FALSE,
    min_advance_days        INT,
    color                   VARCHAR(7),
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255)
);

-- Leave Balances (per employee, per type, per year)
CREATE TABLE leave_balances (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id             UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type_id           UUID NOT NULL REFERENCES leave_types(id) ON DELETE CASCADE,
    year                    INT NOT NULL,
    allocated_days          NUMERIC(5,1) NOT NULL DEFAULT 0,
    carried_forward_days    NUMERIC(5,1) NOT NULL DEFAULT 0,
    used_days               NUMERIC(5,1) NOT NULL DEFAULT 0,
    pending_days            NUMERIC(5,1) NOT NULL DEFAULT 0,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    UNIQUE (employee_id, leave_type_id, year)
);

-- Leave Requests
CREATE TABLE leave_requests (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id             UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type_id           UUID NOT NULL REFERENCES leave_types(id),
    start_date              DATE NOT NULL,
    end_date                DATE NOT NULL,
    total_days              NUMERIC(5,1) NOT NULL,
    day_type                VARCHAR(12) NOT NULL DEFAULT 'FULL'
                                CHECK (day_type IN ('FULL','HALF_FIRST','HALF_SECOND')),
    reason                  TEXT NOT NULL,
    document_url            TEXT,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN ('PENDING','MANAGER_APPROVED','APPROVED','REJECTED','CANCELLED','AUTO_APPROVED')),
    -- Manager
    manager_approver_id     UUID REFERENCES employees(id),
    manager_action_at       TIMESTAMP,
    manager_remarks         VARCHAR(500),
    manager_decision        VARCHAR(10) CHECK (manager_decision IN ('APPROVED','REJECTED')),
    -- HR
    hr_approver_id          UUID REFERENCES employees(id),
    hr_action_at            TIMESTAMP,
    hr_remarks              VARCHAR(500),
    hr_decision             VARCHAR(10) CHECK (hr_decision IN ('APPROVED','REJECTED')),
    -- Cancellation
    cancelled_by_employee   BOOLEAN NOT NULL DEFAULT FALSE,
    cancellation_reason     VARCHAR(500),
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255)
);

CREATE INDEX idx_leave_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_status   ON leave_requests(status);
CREATE INDEX idx_leave_dates    ON leave_requests(start_date, end_date);

-- Seed default leave types for the default company
INSERT INTO leave_types (id, company_id, name, code, max_days_per_year, is_paid, carry_forward_allowed, half_day_allowed, color)
SELECT uuid_generate_v4(), c.id, 'Paid Leave',           'PL',  18, TRUE,  TRUE,  TRUE,  '#10B981' FROM companies c WHERE c.is_deleted = FALSE LIMIT 1;

INSERT INTO leave_types (id, company_id, name, code, max_days_per_year, is_paid, carry_forward_allowed, half_day_allowed, color)
SELECT uuid_generate_v4(), c.id, 'Sick Leave',            'SL',  10, TRUE,  FALSE, TRUE,  '#EF4444' FROM companies c WHERE c.is_deleted = FALSE LIMIT 1;

INSERT INTO leave_types (id, company_id, name, code, max_days_per_year, is_paid, carry_forward_allowed, half_day_allowed, color)
SELECT uuid_generate_v4(), c.id, 'Casual Leave',          'CL',  6,  TRUE,  FALSE, TRUE,  '#F59E0B' FROM companies c WHERE c.is_deleted = FALSE LIMIT 1;

INSERT INTO leave_types (id, company_id, name, code, max_days_per_year, is_paid, carry_forward_allowed, half_day_allowed, color)
SELECT uuid_generate_v4(), c.id, 'Emergency Leave',       'EL',  3,  TRUE,  FALSE, FALSE, '#DC2626' FROM companies c WHERE c.is_deleted = FALSE LIMIT 1;

INSERT INTO leave_types (id, company_id, name, code, max_days_per_year, is_paid, carry_forward_allowed, half_day_allowed, color)
SELECT uuid_generate_v4(), c.id, 'Work From Home',        'WFH', 60, TRUE,  FALSE, FALSE, '#3B82F6' FROM companies c WHERE c.is_deleted = FALSE LIMIT 1;

INSERT INTO leave_types (id, company_id, name, code, max_days_per_year, is_paid, carry_forward_allowed, half_day_allowed, color)
SELECT uuid_generate_v4(), c.id, 'Unpaid Leave',          'UL',  30, FALSE, FALSE, TRUE,  '#6B7280' FROM companies c WHERE c.is_deleted = FALSE LIMIT 1;
