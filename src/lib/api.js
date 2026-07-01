const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const TOKEN_KEY = 'nexstar-access-token';
const REFRESH_TOKEN_KEY = 'nexstar-refresh-token';
const USER_KEY = 'nexstar-auth-user';

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

export async function apiRequest(path, options = {}) {
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

  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

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

export const authApi = {
  login: (credentials) => apiRequest('/auth/login', { method: 'POST', body: credentials }),
  logout: (refreshToken) => apiRequest('/auth/logout', { method: 'POST', body: refreshToken ? { refreshToken } : {} }),
  changePassword: (body) => apiRequest('/auth/change-password', { method: 'POST', body }),
};

export const employeesApi = {
  list: (params) => apiRequest(`/employees${toQuery(params)}`),
  me: () => apiRequest('/employees/me'),
  create: (body) => apiRequest('/employees', { method: 'POST', body }),
  update: (id, body) => apiRequest(`/employees/${id}`, { method: 'PUT', body }),
  terminate: (id, reason) => apiRequest(`/employees/${id}/terminate${toQuery({ reason })}`, { method: 'POST' }),
};

export const attendanceApi = {
  today: () => apiRequest('/attendance/today'),
  my: (params) => apiRequest(`/attendance/my${toQuery(params)}`),
  dashboard: (params) => apiRequest(`/attendance/dashboard${toQuery(params)}`),
  clockIn: (body) => apiRequest('/attendance/clock-in', { method: 'POST', body }),
  clockOut: (body) => apiRequest('/attendance/clock-out', { method: 'POST', body }),
  regularize: (body) => apiRequest('/attendance/regularize', { method: 'POST', body }),
};

export const tasksApi = {
  search: (params) => apiRequest(`/tasks/search${toQuery(params)}`),
  create: (body) => apiRequest('/tasks', { method: 'POST', body }),
  update: (id, body) => apiRequest(`/tasks/${id}`, { method: 'PATCH', body }),
  remove: (id) => apiRequest(`/tasks/${id}`, { method: 'DELETE' }),
};

export const notificationsApi = {
  list: (params) => apiRequest(`/notifications${toQuery(params)}`),
  unreadCount: () => apiRequest('/notifications/unread/count'),
  markRead: (id) => apiRequest(`/notifications/${id}/read`, { method: 'PATCH' }),
  readAll: () => apiRequest('/notifications/read-all', { method: 'POST' }),
};

export const reportsApi = {
  attendance: (params) => apiRequest(`/reports/attendance${toQuery(params)}`),
  employees: (params) => apiRequest(`/reports/employees${toQuery(params)}`),
};

export const organizationApi = {
  createDepartment: (body) => apiRequest('/organization/departments', { method: 'POST', body }),
  departments: (params) => apiRequest(`/organization/departments${toQuery(params)}`),
};
