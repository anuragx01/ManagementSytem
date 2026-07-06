import { baseApi, toQueryParams } from './baseApi';

export const payrollApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getPayroll: builder.query({ query: (params) => ({ url: '/payroll', params: toQueryParams(params) }), providesTags: ['Payroll'] }),
    getMyPayroll: builder.query({ query: () => '/payroll/my', providesTags: ['Payroll'] }),
    createPayroll: builder.mutation({ query: (body) => ({ url: '/payroll', method: 'POST', body }), invalidatesTags: ['Payroll'] }),
    updatePayroll: builder.mutation({ query: ({ id, body }) => ({ url: `/payroll/${id}`, method: 'PATCH', body }), invalidatesTags: ['Payroll'] }),
  }),
});

export const { useGetPayrollQuery, useGetMyPayrollQuery, useCreatePayrollMutation, useUpdatePayrollMutation } = payrollApi;
