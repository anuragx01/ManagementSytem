-- V9: Create task management tables
-- tasks, task_comments, task_checklists, task_watchers, task_activities

-- ── tasks ─────────────────────────────────────────────────────────────────────
CREATE TABLE tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id      UUID NOT NULL REFERENCES projects(id),
    sprint_id       UUID REFERENCES sprints(id),
    parent_id       UUID REFERENCES tasks(id),
    task_key        VARCHAR(20) UNIQUE,
    title           VARCHAR(500) NOT NULL,
    description     TEXT,
    type            VARCHAR(20) NOT NULL DEFAULT 'TASK',
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status          VARCHAR(20) NOT NULL DEFAULT 'BACKLOG',
    assignee_id     UUID REFERENCES employees(id),
    reporter_id     UUID REFERENCES employees(id),
    due_date        DATE,
    story_points    INT NOT NULL DEFAULT 0,
    estimated_hours NUMERIC(8, 2),
    actual_hours    NUMERIC(8, 2),
    position        INT NOT NULL DEFAULT 0,
    is_recurring    BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255)
);

CREATE INDEX idx_tasks_project_id_status ON tasks(project_id, status);
CREATE INDEX idx_tasks_sprint_id         ON tasks(sprint_id);
CREATE INDEX idx_tasks_assignee_id       ON tasks(assignee_id);
CREATE INDEX idx_tasks_due_date          ON tasks(due_date);
CREATE INDEX idx_tasks_task_key          ON tasks(task_key);
CREATE INDEX idx_tasks_parent_id         ON tasks(parent_id);

-- ── task_comments ─────────────────────────────────────────────────────────────
CREATE TABLE task_comments (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id    UUID NOT NULL REFERENCES tasks(id),
    author_id  UUID NOT NULL REFERENCES users(id),
    content    TEXT NOT NULL,
    edited     BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_task_comments_task_id   ON task_comments(task_id);
CREATE INDEX idx_task_comments_author_id ON task_comments(author_id);

-- ── task_checklists ───────────────────────────────────────────────────────────
CREATE TABLE task_checklists (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id    UUID NOT NULL REFERENCES tasks(id),
    title      VARCHAR(200) NOT NULL,
    completed  BOOLEAN NOT NULL DEFAULT FALSE,
    position   INT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_task_checklists_task_id ON task_checklists(task_id);

-- ── task_watchers ─────────────────────────────────────────────────────────────
CREATE TABLE task_watchers (
    task_id  UUID NOT NULL REFERENCES tasks(id),
    user_id  UUID NOT NULL REFERENCES users(id),
    added_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (task_id, user_id)
);

-- ── task_activities ───────────────────────────────────────────────────────────
CREATE TABLE task_activities (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id    UUID NOT NULL REFERENCES tasks(id),
    actor_id   UUID REFERENCES users(id),
    action     VARCHAR(50) NOT NULL,
    from_value VARCHAR(200),
    to_value   VARCHAR(200),
    detail     TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_task_activities_task_id  ON task_activities(task_id);
CREATE INDEX idx_task_activities_actor_id ON task_activities(actor_id);
