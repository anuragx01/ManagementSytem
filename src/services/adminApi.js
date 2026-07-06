import { baseApi, toQueryParams } from './baseApi';

export const adminApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getAuditLogs: builder.query({ query: (params) => ({ url: '/admin/audit-logs', params: toQueryParams(params) }), providesTags: ['Admin'] }),
    getUserAuditLogs: builder.query({ query: ({ userId, params }) => ({ url: `/admin/audit-logs/user/${userId}`, params: toQueryParams(params) }), providesTags: ['Admin'] }),
    getSystemSummary: builder.query({ query: () => '/admin/system/summary', providesTags: ['Admin'] }),
  }),
});

export const { useGetAuditLogsQuery, useGetUserAuditLogsQuery, useGetSystemSummaryQuery } = adminApi;
