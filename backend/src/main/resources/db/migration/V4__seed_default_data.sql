-- ============================================================
-- V4: Seed Default Roles, Permissions & Admin User
-- ============================================================

-- ── Permissions ──────────────────────────────────────────────────────────────

INSERT INTO permissions (id, name, description, module) VALUES
-- Auth
(uuid_generate_v4(), 'MANAGE_USERS',          'Create, update, delete users',          'AUTH'),
(uuid_generate_v4(), 'VIEW_USERS',            'View user list and profiles',           'AUTH'),
(uuid_generate_v4(), 'MANAGE_ROLES',          'Create and assign roles',               'AUTH'),

-- Employee
(uuid_generate_v4(), 'CREATE_EMPLOYEE',       'Add new employees',                     'EMPLOYEE'),
(uuid_generate_v4(), 'UPDATE_EMPLOYEE',       'Edit employee profiles',                'EMPLOYEE'),
(uuid_generate_v4(), 'DELETE_EMPLOYEE',       'Remove employees',                      'EMPLOYEE'),
(uuid_generate_v4(), 'VIEW_EMPLOYEE',         'View employee profiles',                'EMPLOYEE'),
(uuid_generate_v4(), 'VIEW_EMPLOYEE_DOCS',    'Access employee documents',             'EMPLOYEE'),
(uuid_generate_v4(), 'MANAGE_EMPLOYEE_DOCS',  'Upload/delete employee documents',      'EMPLOYEE'),

-- Organization
(uuid_generate_v4(), 'MANAGE_ORGANIZATION',   'Manage company, departments, teams',    'ORGANIZATION'),
(uuid_generate_v4(), 'VIEW_ORGANIZATION',     'View org structure',                    'ORGANIZATION'),

-- Attendance
(uuid_generate_v4(), 'MANAGE_ATTENDANCE',     'Override attendance records',           'ATTENDANCE'),
(uuid_generate_v4(), 'VIEW_ATTENDANCE',       'View attendance reports',               'ATTENDANCE'),
(uuid_generate_v4(), 'VIEW_OWN_ATTENDANCE',   'View own attendance',                   'ATTENDANCE'),

-- Leave
(uuid_generate_v4(), 'APPROVE_LEAVE',         'Approve or reject leave requests',      'LEAVE'),
(uuid_generate_v4(), 'MANAGE_LEAVE',          'Manage leave types and balances',       'LEAVE'),
(uuid_generate_v4(), 'VIEW_LEAVE',            'View all leave requests',               'LEAVE'),
(uuid_generate_v4(), 'APPLY_LEAVE',           'Submit leave requests',                 'LEAVE'),

-- Projects
(uuid_generate_v4(), 'CREATE_PROJECT',        'Create new projects',                   'PROJECT'),
(uuid_generate_v4(), 'UPDATE_PROJECT',        'Edit project details',                  'PROJECT'),
(uuid_generate_v4(), 'DELETE_PROJECT',        'Delete projects',                       'PROJECT'),
(uuid_generate_v4(), 'VIEW_PROJECT',          'View project details',                  'PROJECT'),
(uuid_generate_v4(), 'MANAGE_PROJECT_MEMBERS','Add/remove project members',            'PROJECT'),

-- Tasks
(uuid_generate_v4(), 'CREATE_TASK',           'Create tasks',                          'TASK'),
(uuid_generate_v4(), 'UPDATE_TASK',           'Edit task details',                     'TASK'),
(uuid_generate_v4(), 'DELETE_TASK',           'Delete tasks',                          'TASK'),
(uuid_generate_v4(), 'ASSIGN_TASK',           'Assign tasks to employees',             'TASK'),
(uuid_generate_v4(), 'VIEW_TASK',             'View tasks',                            'TASK'),

-- Time Tracking
(uuid_generate_v4(), 'LOG_TIME',              'Log time entries',                      'TIME'),
(uuid_generate_v4(), 'APPROVE_TIMESHEET',     'Approve timesheets',                    'TIME'),
(uuid_generate_v4(), 'VIEW_TIMESHEETS',       'View all timesheets',                   'TIME'),

-- Reports
(uuid_generate_v4(), 'VIEW_REPORTS',          'Access reporting module',               'REPORTS'),
(uuid_generate_v4(), 'EXPORT_REPORTS',        'Export reports to PDF/Excel',           'REPORTS'),

-- Admin
(uuid_generate_v4(), 'ACCESS_ADMIN',          'Access system administration',          'ADMIN'),
(uuid_generate_v4(), 'VIEW_AUDIT_LOGS',       'View audit and security logs',          'ADMIN'),
(uuid_generate_v4(), 'MANAGE_SETTINGS',       'Manage system settings',                'ADMIN');

-- ── Roles ─────────────────────────────────────────────────────────────────────

INSERT INTO roles (id, name, description, is_system_role) VALUES
(uuid_generate_v4(), 'SUPER_ADMIN', 'Full system access',                         TRUE),
(uuid_generate_v4(), 'HR',          'Human Resources – employee & leave mgmt',    TRUE),
(uuid_generate_v4(), 'MANAGER',     'Manages a team, approves leaves & tasks',    TRUE),
(uuid_generate_v4(), 'TEAM_LEAD',   'Leads a team, manages tasks',                TRUE),
(uuid_generate_v4(), 'EMPLOYEE',    'Standard employee access',                   TRUE);

-- Grant all permissions to SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'SUPER_ADMIN';

-- Grant HR permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.name IN (
    'CREATE_EMPLOYEE','UPDATE_EMPLOYEE','VIEW_EMPLOYEE',
    'VIEW_EMPLOYEE_DOCS','MANAGE_EMPLOYEE_DOCS',
    'MANAGE_ORGANIZATION','VIEW_ORGANIZATION',
    'MANAGE_ATTENDANCE','VIEW_ATTENDANCE',
    'APPROVE_LEAVE','MANAGE_LEAVE','VIEW_LEAVE',
    'VIEW_REPORTS','EXPORT_REPORTS',
    'VIEW_USERS'
) WHERE r.name = 'HR';

-- Grant Manager permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.name IN (
    'VIEW_EMPLOYEE',
    'VIEW_ORGANIZATION',
    'VIEW_ATTENDANCE','VIEW_OWN_ATTENDANCE',
    'APPROVE_LEAVE','VIEW_LEAVE','APPLY_LEAVE',
    'CREATE_PROJECT','UPDATE_PROJECT','VIEW_PROJECT','MANAGE_PROJECT_MEMBERS',
    'CREATE_TASK','UPDATE_TASK','ASSIGN_TASK','VIEW_TASK',
    'LOG_TIME','APPROVE_TIMESHEET','VIEW_TIMESHEETS',
    'VIEW_REPORTS'
) WHERE r.name = 'MANAGER';

-- Grant Team Lead permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.name IN (
    'VIEW_EMPLOYEE',
    'VIEW_ORGANIZATION',
    'VIEW_OWN_ATTENDANCE',
    'APPLY_LEAVE','VIEW_LEAVE',
    'VIEW_PROJECT',
    'CREATE_TASK','UPDATE_TASK','ASSIGN_TASK','VIEW_TASK',
    'LOG_TIME','VIEW_TIMESHEETS'
) WHERE r.name = 'TEAM_LEAD';

-- Grant Employee permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.name IN (
    'VIEW_EMPLOYEE',
    'VIEW_ORGANIZATION',
    'VIEW_OWN_ATTENDANCE',
    'APPLY_LEAVE',
    'VIEW_PROJECT',
    'VIEW_TASK','UPDATE_TASK',
    'LOG_TIME'
) WHERE r.name = 'EMPLOYEE';

-- ── Default Super Admin User ──────────────────────────────────────────────────
-- Password: Admin@123! (BCrypt encoded)
INSERT INTO users (id, email, password, first_name, last_name, is_active, is_email_verified)
VALUES (
    uuid_generate_v4(),
    'admin@nexstar.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN7WiP5KNp9b3V/2DLJKK',
    'Super',
    'Admin',
    TRUE,
    TRUE
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@nexstar.com' AND r.name = 'SUPER_ADMIN';

-- ── Default Company ───────────────────────────────────────────────────────────
INSERT INTO companies (id, name, timezone, currency, date_format)
VALUES (uuid_generate_v4(), 'Nexstar Technologies', 'Asia/Kolkata', 'INR', 'DD/MM/YYYY');
