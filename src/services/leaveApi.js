import { baseApi, toQueryParams } from './baseApi';

export const leaveApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getLeaveTypes: builder.query({ query: (companyId) => ({ url: '/leaves/types', params: toQueryParams({ companyId }) }), providesTags: ['Leave'] }),
    getMyLeaveBalance: builder.query({ query: () => '/leaves/balance/my', providesTags: ['Leave'] }),
    getMyLeaves: builder.query({ query: (params) => ({ url: '/leaves/my', params: toQueryParams(params) }), providesTags: ['Leave'] }),
    applyLeave: builder.mutation({ query: (body) => ({ url: '/leaves/apply', method: 'POST', body }), invalidatesTags: ['Leave'] }),
    getPendingManagerLeaves: builder.query({ query: () => '/leaves/pending/manager', providesTags: ['Leave'] }),
    getPendingHrLeaves: builder.query({ query: () => '/leaves/pending/hr', providesTags: ['Leave'] }),
    approveManagerLeave: builder.mutation({ query: (body) => ({ url: '/leaves/approve/manager', method: 'POST', body }), invalidatesTags: ['Leave'] }),
    approveHrLeave: builder.mutation({ query: (body) => ({ url: '/leaves/approve/hr', method: 'POST', body }), invalidatesTags: ['Leave'] }),
    getLeaveCalendar: builder.query({ query: (params) => ({ url: '/leaves/calendar', params: toQueryParams(params) }), providesTags: ['Leave'] }),
  }),
});

export const { useGetLeaveTypesQuery, useGetMyLeaveBalanceQuery, useGetMyLeavesQuery, useApplyLeaveMutation, useGetPendingManagerLeavesQuery, useGetPendingHrLeavesQuery, useApproveManagerLeaveMutation, useApproveHrLeaveMutation, useGetLeaveCalendarQuery } = leaveApi;
