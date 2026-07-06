import { baseApi, toQueryParams } from './baseApi';

export const reportApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getAttendanceReport: builder.query({ query: (params) => ({ url: '/reports/attendance', params: toQueryParams(params) }), providesTags: ['Report'] }),
    getLeaveReport: builder.query({ query: (params) => ({ url: '/reports/leave', params: toQueryParams(params) }), providesTags: ['Report'] }),
    getEmployeeReport: builder.query({ query: (params) => ({ url: '/reports/employees', params: toQueryParams(params) }), providesTags: ['Report'] }),
    getProjectReport: builder.query({ query: (projectId) => `/reports/project/${projectId}`, providesTags: ['Report'] }),
  }),
});

export const { useGetAttendanceReportQuery, useGetLeaveReportQuery, useGetEmployeeReportQuery, useGetProjectReportQuery } = reportApi;
