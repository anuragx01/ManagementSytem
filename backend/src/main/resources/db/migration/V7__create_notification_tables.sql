-- ============================================================
-- V7: Notifications Table
-- ============================================================

CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        VARCHAR(30) NOT NULL
                    CHECK (type IN (
                        'TASK_ASSIGNED','TASK_UPDATED','TASK_COMMENT','TASK_MENTIONED',
                        'TASK_DEADLINE','LEAVE_APPLIED','LEAVE_APPROVED','LEAVE_REJECTED',
                        'LEAVE_CANCELLED','ATTENDANCE_REMINDER','ANNOUNCEMENT','SYSTEM'
                    )),
    title       VARCHAR(200) NOT NULL,
    message     TEXT NOT NULL,
    action_url  VARCHAR(500),
    entity_id   UUID,
    entity_type VARCHAR(30),
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE INDEX idx_notif_recipient ON notifications(recipient_id);
CREATE INDEX idx_notif_read      ON notifications(is_read);
CREATE INDEX idx_notif_type      ON notifications(type);
