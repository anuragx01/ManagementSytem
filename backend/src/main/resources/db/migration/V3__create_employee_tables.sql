-- ============================================================
-- V3: Employee Management Tables
-- ============================================================

-- Employees
CREATE TABLE employees (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID UNIQUE REFERENCES users(id),
    employee_id         VARCHAR(20) UNIQUE,
    date_of_birth       DATE,
    gender              VARCHAR(20) CHECK (gender IN ('MALE','FEMALE','OTHER','PREFER_NOT_TO_SAY')),
    nationality         VARCHAR(100),
    marital_status      VARCHAR(20),
    blood_group         VARCHAR(5),
    personal_email      VARCHAR(100),
    phone_number        VARCHAR(20),
    alternate_phone     VARCHAR(20),
    address             TEXT,
    city                VARCHAR(100),
    state               VARCHAR(100),
    country             VARCHAR(100),
    postal_code         VARCHAR(20),
    department_id       UUID REFERENCES departments(id),
    team_id             UUID REFERENCES teams(id),
    designation_id      UUID REFERENCES designations(id),
    branch_id           UUID REFERENCES branches(id),
    reporting_manager_id UUID REFERENCES employees(id),
    date_of_joining     DATE NOT NULL,
    date_of_leaving     DATE,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE','INACTIVE','ON_LEAVE','TERMINATED','RESIGNED','NOTICE_PERIOD')),
    employment_type     VARCHAR(20) CHECK (employment_type IN ('FULL_TIME','PART_TIME','CONTRACT','INTERN','CONSULTANT')),
    work_location       VARCHAR(20),
    profile_picture_url TEXT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

CREATE INDEX idx_employees_employee_id ON employees(employee_id);
CREATE INDEX idx_employees_user_id ON employees(user_id);
CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_status ON employees(status);

-- Emergency Contacts
CREATE TABLE emergency_contacts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id     UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    relationship    VARCHAR(50) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    alternate_phone VARCHAR(20),
    email           VARCHAR(100),
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255)
);

-- Employee Documents
CREATE TABLE employee_documents (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    type        VARCHAR(30) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    file_url    TEXT NOT NULL,
    file_size   BIGINT,
    file_type   VARCHAR(50),
    notes       TEXT,
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

-- Employee Skills
CREATE TABLE employee_skills (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id         UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    name                VARCHAR(100) NOT NULL,
    proficiency         VARCHAR(20) CHECK (proficiency IN ('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT')),
    years_of_experience INT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

-- Employment History
CREATE TABLE employment_history (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id         UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    company_name        VARCHAR(200) NOT NULL,
    designation         VARCHAR(100) NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE,
    is_current          BOOLEAN NOT NULL DEFAULT FALSE,
    responsibilities    TEXT,
    reason_for_leaving  VARCHAR(200),
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);
