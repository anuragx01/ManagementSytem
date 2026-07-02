-- ============================================================
-- V5: Attendance Management Tables
-- ============================================================

-- Attendance Policies
CREATE TABLE attendance_policies (
    id                          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id                  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name                        VARCHAR(100) NOT NULL,
    office_start_time           TIME NOT NULL,
    office_end_time             TIME NOT NULL,
    grace_time_minutes          INT NOT NULL DEFAULT 15,
    half_day_hours              INT NOT NULL DEFAULT 4,
    full_day_hours              INT NOT NULL DEFAULT 8,
    overtime_threshold_minutes  INT NOT NULL DEFAULT 30,
    working_days_mask           INT NOT NULL DEFAULT 62,
    allow_work_from_home        BOOLEAN NOT NULL DEFAULT TRUE,
    require_location            BOOLEAN NOT NULL DEFAULT FALSE,
    is_default                  BOOLEAN NOT NULL DEFAULT FALSE,
    is_active                   BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted                  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP,
    created_by                  VARCHAR(255),
    updated_by                  VARCHAR(255)
);

-- Attendance Records (one per employee per day)
CREATE TABLE attendance_records (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id         UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    policy_id           UUID REFERENCES attendance_policies(id),
    date                DATE NOT NULL,
    clock_in            TIMESTAMP,
    clock_out           TIMESTAMP,
    total_break_minutes INT NOT NULL DEFAULT 0,
    worked_minutes      INT,
    late_minutes        INT NOT NULL DEFAULT 0,
    early_exit_minutes  INT NOT NULL DEFAULT 0,
    overtime_minutes    INT NOT NULL DEFAULT 0,
    status              VARCHAR(20) NOT NULL DEFAULT 'ABSENT'
                            CHECK (status IN ('PRESENT','LATE','HALF_DAY','ABSENT','ON_LEAVE','HOLIDAY','WEEKEND','WORK_FROM_HOME')),
    work_mode           VARCHAR(10) CHECK (work_mode IN ('OFFICE','WFH','HYBRID')),
    clock_in_location   VARCHAR(200),
    clock_out_location  VARCHAR(200),
    remarks             VARCHAR(500),
    is_regularized      BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    UNIQUE (employee_id, date)
);

CREATE INDEX idx_att_employee_date ON attendance_records(employee_id, date);
CREATE INDEX idx_att_date          ON attendance_records(date);
CREATE INDEX idx_att_status        ON attendance_records(status);

-- Attendance Breaks
CREATE TABLE attendance_breaks (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    attendance_record_id    UUID NOT NULL REFERENCES attendance_records(id) ON DELETE CASCADE,
    break_start             TIMESTAMP NOT NULL,
    break_end               TIMESTAMP,
    duration_minutes        INT,
    type                    VARCHAR(20) NOT NULL DEFAULT 'BREAK'
                                CHECK (type IN ('BREAK','LUNCH','PERSONAL')),
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255)
);

-- Default attendance policy seed
INSERT INTO attendance_policies (
    id, company_id, name, office_start_time, office_end_time,
    grace_time_minutes, half_day_hours, full_day_hours,
    overtime_threshold_minutes, working_days_mask,
    allow_work_from_home, require_location, is_default
)
SELECT
    uuid_generate_v4(), c.id, 'Standard Policy', '09:00', '18:00',
    15, 4, 8, 30, 62, TRUE, FALSE, TRUE
FROM companies c
WHERE c.is_deleted = FALSE
LIMIT 1;
