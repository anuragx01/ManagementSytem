import { baseApi, toQueryParams } from './baseApi';

export const attendanceApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getTodayAttendance: builder.query({ query: () => '/attendance/today', providesTags: ['Attendance'] }),
    getMyAttendance: builder.query({ query: (params) => ({ url: '/attendance/my', params: toQueryParams(params) }), providesTags: ['Attendance'] }),
    getAllAttendance: builder.query({ query: (params) => ({ url: '/attendance/all', params: toQueryParams(params) }), providesTags: ['Attendance'] }),
    getAttendanceDashboard: builder.query({ query: (params) => ({ url: '/attendance/dashboard', params: toQueryParams(params) }), providesTags: ['Attendance'] }),
    clockIn: builder.mutation({ query: (body = {}) => ({ url: '/attendance/clock-in', method: 'POST', body }), invalidatesTags: ['Attendance'] }),
    clockOut: builder.mutation({ query: (body = {}) => ({ url: '/attendance/clock-out', method: 'POST', body }), invalidatesTags: ['Attendance'] }),
    regularizeAttendance: builder.mutation({ query: (body) => ({ url: '/attendance/regularize', method: 'POST', body }), invalidatesTags: ['Attendance'] }),
  }),
});

export const { useGetTodayAttendanceQuery, useGetMyAttendanceQuery, useGetAllAttendanceQuery, useGetAttendanceDashboardQuery, useClockInMutation, useClockOutMutation, useRegularizeAttendanceMutation } = attendanceApi;
