-- V8: Create project management tables
-- projects, project_members, milestones, sprints, project_labels

-- ── projects ──────────────────────────────────────────────────────────────────
CREATE TABLE projects (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id),
    owner_id        UUID REFERENCES employees(id),
    name            VARCHAR(200) NOT NULL,
    project_key     VARCHAR(10)  NOT NULL,
    description     TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PLANNING',
    type            VARCHAR(20),
    start_date      DATE,
    end_date        DATE,
    logo_url        TEXT,
    is_private      BOOLEAN NOT NULL DEFAULT FALSE,
    archived        BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    CONSTRAINT uq_project_key_company UNIQUE (project_key, company_id)
);

CREATE INDEX idx_projects_company_id ON projects(company_id);
CREATE INDEX idx_projects_owner_id   ON projects(owner_id);
CREATE INDEX idx_projects_status     ON projects(status);
CREATE INDEX idx_projects_key        ON projects(project_key);

-- ── project_members ───────────────────────────────────────────────────────────
CREATE TABLE project_members (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id  UUID NOT NULL REFERENCES projects(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    role        VARCHAR(20) NOT NULL DEFAULT 'DEVELOPER',
    joined_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT uq_project_member UNIQUE (project_id, employee_id)
);

CREATE INDEX idx_project_members_project_id  ON project_members(project_id);
CREATE INDEX idx_project_members_employee_id ON project_members(employee_id);

-- ── milestones ────────────────────────────────────────────────────────────────
CREATE TABLE milestones (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id            UUID NOT NULL REFERENCES projects(id),
    name                  VARCHAR(200) NOT NULL,
    description           TEXT,
    start_date            DATE,
    due_date              DATE,
    status                VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    completion_percentage INT NOT NULL DEFAULT 0,
    is_deleted            BOOLEAN NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255)
);

CREATE INDEX idx_milestones_project_id ON milestones(project_id);
CREATE INDEX idx_milestones_status     ON milestones(status);

-- ── sprints ───────────────────────────────────────────────────────────────────
CREATE TABLE sprints (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id   UUID NOT NULL REFERENCES projects(id),
    milestone_id UUID REFERENCES milestones(id),
    name         VARCHAR(100) NOT NULL,
    goal         TEXT,
    start_date   DATE NOT NULL,
    end_date     DATE NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    velocity     INT NOT NULL DEFAULT 0,
    is_deleted   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255)
);

CREATE INDEX idx_sprints_project_id   ON sprints(project_id);
CREATE INDEX idx_sprints_milestone_id ON sprints(milestone_id);
CREATE INDEX idx_sprints_status       ON sprints(status);

-- ── project_labels ────────────────────────────────────────────────────────────
CREATE TABLE project_labels (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    name       VARCHAR(50) NOT NULL,
    color      VARCHAR(7),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_project_labels_project_id ON project_labels(project_id);
