import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { credentialsReceived, loggedOut } from '../features/auth/authSlice';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const REFRESH_TOKEN_KEY = 'nexstar-refresh-token';
const SERVER_UNAVAILABLE_MESSAGE = 'Unable to connect to the server. Please make sure the backend is running.';

let refreshPromise = null;

function unwrapResponse(payload) {
  if (payload && typeof payload === 'object' && 'data' in payload) return payload.data;
  return payload;
}

function getErrorMessage(error) {
  if (error?.status === 'FETCH_ERROR' || /failed to fetch/i.test(error?.error || '')) {
    return SERVER_UNAVAILABLE_MESSAGE;
  }

  const data = error?.data;
  if (typeof data === 'string') return data;
  return data?.error || data?.message || error?.error || 'Request failed.';
}

function getRequestUrl(args) {
  const path = typeof args === 'string' ? args : args?.url || '';
  const base = API_BASE_URL.replace(/\/$/, '');
  const normalizedPath = String(path).startsWith('/') ? path : `/${path}`;
  return `${base}${normalizedPath}`;
}

function sanitizeRequestBody(body) {
  if (!body) return body;
  if (body instanceof FormData) return '[FormData]';
  if (typeof body !== 'object' || Array.isArray(body)) return body;

  return Object.fromEntries(
    Object.entries(body).map(([key, value]) => [
      key,
      /password|token/i.test(key) ? '[redacted]' : value,
    ]),
  );
}

function logRequest(args) {
  if (!import.meta.env.DEV) return;

  const method = typeof args === 'string' ? 'GET' : args?.method || 'GET';
  const body = typeof args === 'string' ? undefined : args?.body;

  console.info('[api request]', {
    url: getRequestUrl(args),
    method,
    body: sanitizeRequestBody(body),
  });
}

const rawBaseQuery = fetchBaseQuery({
  baseUrl: API_BASE_URL,
  prepareHeaders: (headers, { getState }) => {
    const token = getState().auth?.accessToken || localStorage.getItem('nexstar-access-token');
    if (token) headers.set('authorization', `Bearer ${token}`);
    return headers;
  },
});

const baseQueryWithReauth = async (args, api, extraOptions) => {
  logRequest(args);

  let result = await rawBaseQuery(args, api, extraOptions);
  const url = typeof args === 'string' ? args : args?.url;
  const isAuthEndpoint = String(url || '').startsWith('/auth/');

  if (result.error?.status === 401 && !isAuthEndpoint) {
    const refreshToken = api.getState().auth?.refreshToken || localStorage.getItem(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      api.dispatch(loggedOut());
      return result;
    }

    if (!refreshPromise) {
      refreshPromise = rawBaseQuery(
        { url: '/auth/refresh-token', method: 'POST', body: { refreshToken } },
        api,
        extraOptions,
      ).finally(() => {
        refreshPromise = null;
      });
    }

    const refreshResult = await refreshPromise;
    if (refreshResult.data) {
      const refreshed = unwrapResponse(refreshResult.data);
      const currentUser = api.getState().auth?.user || {};
      api.dispatch(credentialsReceived({ ...currentUser, ...refreshed }));
      result = await rawBaseQuery(args, api, extraOptions);
    } else {
      api.dispatch(loggedOut());
    }
  }

  if (result.data !== undefined) {
    result.data = unwrapResponse(result.data);
  }
  if (result.error) {
    if (import.meta.env.DEV) {
      console.error('[api error]', result.error);
    }
    result.error = { ...result.error, message: getErrorMessage(result.error) };
  }
  return result;
};

export const baseApi = createApi({
  reducerPath: 'baseApi',
  baseQuery: baseQueryWithReauth,
  tagTypes: [
    'Auth', 'Profile', 'Employee', 'Department', 'Organization', 'Notification',
    'Attendance', 'Leave', 'Task', 'Project', 'Payroll', 'Report',
    'Announcement', 'DailyReport', 'Admin', 'Document',
  ],
  endpoints: () => ({}),
});

export function pageContent(data, fallback = []) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  return fallback;
}

export function toQueryParams(params = {}) {
  return Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''));
}

