const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const TOKEN_KEY = 'nexstar-access-token';
const REFRESH_TOKEN_KEY = 'nexstar-refresh-token';
const USER_KEY = 'nexstar-auth-user';

let refreshPromise = null;

export function getStoredAuth() {
  const user = localStorage.getItem(USER_KEY);
  return {
    accessToken: localStorage.getItem(TOKEN_KEY),
    refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY),
    user: user ? JSON.parse(user) : null,
  };
}

export function storeAuth(authData) {
  if (authData.accessToken) localStorage.setItem(TOKEN_KEY, authData.accessToken);
  if (authData.refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, authData.refreshToken);
  localStorage.setItem(USER_KEY, JSON.stringify(authData));
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

function unwrapResponse(payload) {
  if (payload && typeof payload === 'object' && 'data' in payload) {
    return payload.data;
  }
  return payload;
}

function toQuery(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.set(key, value);
  });
  const value = query.toString();
  return value ? `?${value}` : '';
}

async function parseResponse(response) {
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

async function refreshAccessToken() {
  if (refreshPromise) return refreshPromise;

  refreshPromise = (async () => {
    const { refreshToken, user } = getStoredAuth();
    if (!refreshToken) throw new Error('Session expired. Please sign in again.');

    const response = await fetch(`${API_BASE_URL}/auth/refresh-token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    const payload = await parseResponse(response);
    if (!response.ok || payload?.success === false) {
      clearAuth();
      throw new Error(payload?.error || payload?.message || 'Session expired. Please sign in again.');
    }

    const data = unwrapResponse(payload);
    storeAuth({ ...user, ...data });
    return data.accessToken;
  })();

  try {
    return await refreshPromise;
  } finally {
    refreshPromise = null;
  }
}

export async function apiRequest(path, options = {}, retried = false) {
  const token = getStoredAuth().accessToken;
  const headers = {
    ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    body: options.body && !(options.body instanceof FormData) ? JSON.stringify(options.body) : options.body,
  });

  const payload = await parseResponse(response);

  if (response.status === 401 && token && !retried && !path.startsWith('/auth/')) {
    await refreshAccessToken();
    return apiRequest(path, options, true);
  }

  if (!response.ok || payload?.success === false) {
    throw new Error(payload?.error || payload?.message || `Request failed with status ${response.status}`);
  }

  return unwrapResponse(payload);
}

export function pageContent(data, fallback = []) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  return fallback;
}

export function pageMeta(data) {
  return {
    content: pageContent(data),
    totalElements: data?.totalElements ?? pageContent(data).length,
    totalPages: data?.totalPages ?? 1,
    page: data?.number ?? data?.page ?? 0,
    size: data?.size ?? pageContent(data).length,
  };
}

// ─── Authentication ───────────────────────────────────────────────────────────

export const authApi = {
  login: (credentials) => apiRequest('/auth/login', { method: 'POST', body: credentials }),
  register: (body) => apiRequest('/auth/register', { method: 'POST', body }),
  refreshToken: (refreshToken) => apiRequest('/auth/refresh-token', { method: 'POST', body: { refreshToken } }),
  logout: (refreshToken) => apiRequest('/auth/logout', { method: 'POST', body: refreshToken ? { refreshToken } : {} }),
  forgotPassword: (email) => apiRequest('/auth/forgot-password', { method: 'POST', body: { email } }),
  resetPassword: (body) => apiRequest('/auth/reset-password', { method: 'POST', body }),
  verifyEmail: (token) => apiRequest(`/auth/verify-email${toQuery({ token })}`),
  resendVerification: (email) => apiRequest(`/auth/resend-verification${toQuery({ email })}`, { method: 'POST' }),
  changePassword: (body) => apiRequest('/auth/change-password', { method: 'POST', body }),
};

// ─── Organization ─────────────────────────────────────────────────────────────

export const organizationApi = {
  company: () => apiRequest('/organization/company'),
  upsertCompany: (body) => apiRequest('/organization/company', { method: 'PUT', body }),
  departments: (params) => apiRequest(`/organization/departments${toQuery(params)}`),
  createDepartment: (body) => apiRequest('/organization/departments', { method: 'POST', body }),
  updateDepartment: (id, body) => apiRequest(`/organization/departments/${id}`, { method: 'PUT', body }),
  deleteDepartment: (id) => apiRequest(`/organization/departments/${id}`, { method: 'DELETE' }),
  teams: (departmentId) => apiRequest(`/organization/departments/${departmentId}/teams`),
  createTeam: (body) => apiRequest('/organization/teams', { method: 'POST', body }),
  updateTeam: (id, body) => apiRequest(`/organization/teams/${id}`, { method: 'PUT', body }),
  deleteTeam: (id) => apiRequest(`/organization/teams/${id}`, { method: 'DELETE' }),
  designations: (params) => apiRequest(`/organization/designations${toQuery(params)}`),
  createDesignation: (body) => apiRequest('/organization/designations', { method: 'POST', body }),
  updateDesignation: (id, body) => apiRequest(`/organization/designations/${id}`, { method: 'PUT', body }),
  deleteDesignation: (id) => apiRequest(`/organization/designations/${id}`, { method: 'DELETE' }),
  branches: (params) => apiRequest(`/organization/branches${toQuery(params)}`),
  createBranch: (body) => apiRequest('/organization/branches', { method: 'POST', body }),
  updateBranch: (id, body) => apiRequest(`/organization/branches/${id}`, { method: 'PUT', body }),
  deleteBranch: (id) => apiRequest(`/organization/branches/${id}`, { method: 'DELETE' }),
  holidays: (params) => apiRequest(`/organization/holidays${toQuery(params)}`),
  createHoliday: (body) => apiRequest('/organization/holidays', { method: 'POST', body }),
  updateHoliday: (id, body) => apiRequest(`/organization/holidays/${id}`, { method: 'PUT', body }),
  deleteHoliday: (id) => apiRequest(`/organization/holidays/${id}`, { method: 'DELETE' }),
};

// ─── Employees ────────────────────────────────────────────────────────────────

export const employeesApi = {
  list: (params) => apiRequest(`/employees${toQuery(params)}`),
  get: (id) => apiRequest(`/employees/${id}`),
  me: () => apiRequest('/employees/me'),
  create: (body) => apiRequest('/employees', { method: 'POST', body }),
  update: (id, body) => apiRequest(`/employees/${id}`, { method: 'PUT', body }),
  updateMe: (id, body) => apiRequest(`/employees/${id}`, { method: 'PUT', body }),
  terminate: (id, reason) => apiRequest(`/employees/${id}/terminate${toQuery({ reason })}`, { method: 'POST' }),
  emergencyContacts: (id) => apiRequest(`/employees/${id}/emergency-contacts`),
  addEmergencyContact: (id, body) => apiRequest(`/employees/${id}/emergency-contacts`, { method: 'POST', body }),
  removeEmergencyContact: (contactId) => apiRequest(`/employees/emergency-contacts/${contactId}`, { method: 'DELETE' }),
  skills: (id) => apiRequest(`/employees/${id}/skills`),
  addSkill: (id, body) => apiRequest(`/employees/${id}/skills`, { method: 'POST', body }),
  removeSkill: (skillId) => apiRequest(`/employees/skills/${skillId}`, { method: 'DELETE' }),
  employmentHistory: (id) => apiRequest(`/employees/${id}/employment-history`),
  addEmploymentHistory: (id, body) => apiRequest(`/employees/${id}/employment-history`, { method: 'POST', body }),
};

// ─── Attendance ───────────────────────────────────────────────────────────────

export const attendanceApi = {
  clockIn: (body = {}) => apiRequest('/attendance/clock-in', { method: 'POST', body }),
  clockOut: (body = {}) => apiRequest('/attendance/clock-out', { method: 'POST', body }),
  startBreak: () => apiRequest('/attendance/break/start', { method: 'POST' }),
  endBreak: () => apiRequest('/attendance/break/end', { method: 'POST' }),
  today: () => apiRequest('/attendance/today'),
  my: (params) => apiRequest(`/attendance/my${toQuery(params)}`),
  all: (params) => apiRequest(`/attendance/all${toQuery(params)}`),
  employee: (employeeId, params) => apiRequest(`/attendance/employee/${employeeId}${toQuery(params)}`),
  dashboard: (params) => apiRequest(`/attendance/dashboard${toQuery(params)}`),
  regularize: (body) => apiRequest('/attendance/regularize', { method: 'POST', body }),
  policies: (companyId) => apiRequest(`/attendance/policies${toQuery({ companyId })}`),
  createPolicy: (body) => apiRequest('/attendance/policies', { method: 'POST', body }),
};

// ─── Leave Management ─────────────────────────────────────────────────────────

export const leaveApi = {
  types: (companyId) => apiRequest(`/leaves/types${toQuery({ companyId })}`),
  createType: (body) => apiRequest('/leaves/types', { method: 'POST', body }),
  updateType: (id, body) => apiRequest(`/leaves/types/${id}`, { method: 'PUT', body }),
  deleteType: (id) => apiRequest(`/leaves/types/${id}`, { method: 'DELETE' }),
  myBalance: () => apiRequest('/leaves/balance/my'),
  employeeBalance: (employeeId, year) => apiRequest(`/leaves/balance/employee/${employeeId}${toQuery({ year })}`),
  allocateBalance: (params) => apiRequest(`/leaves/balance/allocate${toQuery(params)}`, { method: 'POST' }),
  apply: (body) => apiRequest('/leaves/apply', { method: 'POST', body }),
  my: (params) => apiRequest(`/leaves/my${toQuery(params)}`),
  cancel: (id, reason) => apiRequest(`/leaves/cancel/${id}${toQuery({ reason })}`, { method: 'POST' }),
  pendingManager: () => apiRequest('/leaves/pending/manager'),
  pendingHr: () => apiRequest('/leaves/pending/hr'),
  approveManager: (body) => apiRequest('/leaves/approve/manager', { method: 'POST', body }),
  approveHr: (body) => apiRequest('/leaves/approve/hr', { method: 'POST', body }),
  calendar: (params) => apiRequest(`/leaves/calendar${toQuery(params)}`),
  teamCalendar: (teamId, params) => apiRequest(`/leaves/calendar/team/${teamId}${toQuery(params)}`),
};

// ─── Projects ─────────────────────────────────────────────────────────────────

export const projectsApi = {
  list: (params) => apiRequest(`/projects${toQuery(params)}`),
  get: (id) => apiRequest(`/projects/${id}`),
  create: (body) => apiRequest('/projects', { method: 'POST', body }),
  update: (id, body) => apiRequest(`/projects/${id}`, { method: 'PUT', body }),
  archive: (id) => apiRequest(`/projects/${id}/archive`, { method: 'POST' }),
  members: (projectId) => apiRequest(`/projects/${projectId}/members`),
  addMember: (projectId, params) => apiRequest(`/projects/${projectId}/members${toQuery(params)}`, { method: 'POST' }),
  removeMember: (projectId, employeeId) => apiRequest(`/projects/${projectId}/members/${employeeId}`, { method: 'DELETE' }),
  milestones: (projectId) => apiRequest(`/projects/${projectId}/milestones`),
  createMilestone: (projectId, body) => apiRequest(`/projects/${projectId}/milestones`, { method: 'POST', body }),
  updateMilestone: (projectId, milestoneId, body) => apiRequest(`/projects/${projectId}/milestones/${milestoneId}`, { method: 'PUT', body }),
  sprints: (projectId) => apiRequest(`/projects/${projectId}/sprints`),
  activeSprint: (projectId) => apiRequest(`/projects/${projectId}/sprints/active`),
  createSprint: (projectId, body) => apiRequest(`/projects/${projectId}/sprints`, { method: 'POST', body }),
  startSprint: (sprintId) => apiRequest(`/projects/sprints/${sprintId}/start`, { method: 'POST' }),
  completeSprint: (sprintId) => apiRequest(`/projects/sprints/${sprintId}/complete`, { method: 'POST' }),
};

// ─── Tasks ────────────────────────────────────────────────────────────────────

export const tasksApi = {
  search: (params) => apiRequest(`/tasks/search${toQuery(params)}`),
  get: (id) => apiRequest(`/tasks/${id}`),
  create: (body) => apiRequest('/tasks', { method: 'POST', body }),
  update: (id, body) => apiRequest(`/tasks/${id}`, { method: 'PATCH', body }),
  remove: (id) => apiRequest(`/tasks/${id}`, { method: 'DELETE' }),
  kanban: (params) => apiRequest(`/tasks/kanban${toQuery(params)}`),
  backlog: (projectId) => apiRequest(`/tasks/backlog${toQuery({ projectId })}`),
  subtasks: (id) => apiRequest(`/tasks/${id}/subtasks`),
  addComment: (taskId, content) => apiRequest(`/tasks/${taskId}/comments${toQuery({ content })}`, { method: 'POST' }),
  comments: (taskId) => apiRequest(`/tasks/${taskId}/comments`),
  removeComment: (commentId) => apiRequest(`/tasks/comments/${commentId}`, { method: 'DELETE' }),
  replaceChecklists: (taskId, items) => apiRequest(`/tasks/${taskId}/checklists`, { method: 'PUT', body: items }),
  toggleChecklist: (checklistId) => apiRequest(`/tasks/checklists/${checklistId}/toggle`, { method: 'POST' }),
  watch: (taskId) => apiRequest(`/tasks/${taskId}/watch`, { method: 'POST' }),
  unwatch: (taskId) => apiRequest(`/tasks/${taskId}/watch`, { method: 'DELETE' }),
  activity: (taskId, params) => apiRequest(`/tasks/${taskId}/activity${toQuery(params)}`),
};

// ─── Time Tracking ────────────────────────────────────────────────────────────

export const timeApi = {
  startTimer: (body) => apiRequest('/time/timer/start', { method: 'POST', body }),
  pauseTimer: () => apiRequest('/time/timer/pause', { method: 'POST' }),
  resumeTimer: () => apiRequest('/time/timer/resume', { method: 'POST' }),
  stopTimer: () => apiRequest('/time/timer/stop', { method: 'POST' }),
  timerStatus: () => apiRequest('/time/timer/status'),
  log: (body) => apiRequest('/time/log', { method: 'POST', body }),
  entries: (params) => apiRequest(`/time/entries${toQuery(params)}`),
  weeklySummary: (params) => apiRequest(`/time/weekly${toQuery(params)}`),
  submitTimesheet: (params) => apiRequest(`/time/timesheet/submit${toQuery(params)}`, { method: 'POST' }),
  approveTimesheet: (id, params) => apiRequest(`/time/timesheet/${id}/approve${toQuery(params)}`, { method: 'POST' }),
  pendingTimesheets: () => apiRequest('/time/timesheet/pending'),
  myTimesheets: (params) => apiRequest(`/time/timesheet/my${toQuery(params)}`),
};

// ─── Notifications ──────────────────────────────────────────────────────────

export const notificationsApi = {
  list: (params) => apiRequest(`/notifications${toQuery(params)}`),
  unread: (params) => apiRequest(`/notifications/unread${toQuery(params)}`),
  unreadCount: () => apiRequest('/notifications/unread/count'),
  archived: (params) => apiRequest(`/notifications/archived${toQuery(params)}`),
  markRead: (id) => apiRequest(`/notifications/${id}/read`, { method: 'PATCH' }),
  readAll: () => apiRequest('/notifications/read-all', { method: 'POST' }),
  archive: (id) => apiRequest(`/notifications/${id}/archive`, { method: 'PATCH' }),
  remove: (id) => apiRequest(`/notifications/${id}`, { method: 'DELETE' }),
};

// ─── Documents ────────────────────────────────────────────────────────────────

export const documentsApi = {
  uploadEmployeeDocument: (employeeId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiRequest(`/documents/employee/${employeeId}/upload`, { method: 'POST', body: formData });
  },
  uploadTaskAttachment: (taskId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiRequest(`/documents/task/${taskId}/upload`, { method: 'POST', body: formData });
  },
  uploadProfilePicture: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiRequest('/documents/profile-picture', { method: 'POST', body: formData });
  },
  presignedUrl: (key) => apiRequest(`/documents/presigned-url${toQuery({ key })}`),
};

// ─── Reports ──────────────────────────────────────────────────────────────────

export const reportsApi = {
  attendance: (params) => apiRequest(`/reports/attendance${toQuery(params)}`),
  leave: (params) => apiRequest(`/reports/leave${toQuery(params)}`),
  employees: (params) => apiRequest(`/reports/employees${toQuery(params)}`),
  project: (projectId) => apiRequest(`/reports/project/${projectId}`),
};

export const payrollApi = {
  list: (params) => apiRequest(`/payroll${toQuery(params)}`),
  my: () => apiRequest('/payroll/my'),
  create: (body) => apiRequest('/payroll', { method: 'POST', body }),
  update: (id, body) => apiRequest(`/payroll/${id}`, { method: 'PATCH', body }),
  remove: (id) => apiRequest(`/payroll/${id}`, { method: 'DELETE' }),
};

export const announcementsApi = {
  list: (params) => apiRequest(`/announcements${toQuery(params)}`),
  create: (body) => apiRequest('/announcements', { method: 'POST', body }),
  update: (id, body) => apiRequest(`/announcements/${id}`, { method: 'PUT', body }),
  remove: (id) => apiRequest(`/announcements/${id}`, { method: 'DELETE' }),
};

export const dailyReportsApi = {
  my: () => apiRequest('/daily-reports/my'),
  all: () => apiRequest('/daily-reports'),
  submitMine: (body) => apiRequest('/daily-reports/my', { method: 'POST', body }),
};

// ─── Admin ────────────────────────────────────────────────────────────────────

export const adminApi = {
  auditLogs: (params) => apiRequest(`/admin/audit-logs${toQuery(params)}`),
  userAuditLogs: (userId, params) => apiRequest(`/admin/audit-logs/user/${userId}${toQuery(params)}`),
  systemSummary: () => apiRequest('/admin/system/summary'),
};

