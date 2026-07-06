import { baseApi, toQueryParams } from './baseApi';

export const employeeApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getEmployees: builder.query({
      query: (params) => ({ url: '/employees', params: toQueryParams(params) }),
      providesTags: ['Employee'],
    }),
    getEmployee: builder.query({ query: (id) => `/employees/${id}`, providesTags: ['Employee'] }),
    getMe: builder.query({ query: () => '/employees/me', providesTags: ['Profile', 'Employee'] }),
    createEmployee: builder.mutation({ query: (body) => ({ url: '/employees', method: 'POST', body }), invalidatesTags: ['Employee'] }),
    updateEmployee: builder.mutation({ query: ({ id, body }) => ({ url: `/employees/${id}`, method: 'PUT', body }), invalidatesTags: ['Employee', 'Profile'] }),
    terminateEmployee: builder.mutation({ query: ({ id, reason }) => ({ url: `/employees/${id}/terminate`, method: 'POST', params: toQueryParams({ reason }) }), invalidatesTags: ['Employee'] }),
  }),
});

export const { useGetEmployeesQuery, useGetEmployeeQuery, useGetMeQuery, useCreateEmployeeMutation, useUpdateEmployeeMutation, useTerminateEmployeeMutation } = employeeApi;
