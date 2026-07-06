import { baseApi, toQueryParams } from './baseApi';

export const organizationApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getCompany: builder.query({ query: () => '/organization/company', providesTags: ['Organization'] }),
    getDepartments: builder.query({ query: (params) => ({ url: '/organization/departments', params: toQueryParams(params) }), providesTags: ['Department'] }),
    createDepartment: builder.mutation({ query: (body) => ({ url: '/organization/departments', method: 'POST', body }), invalidatesTags: ['Department'] }),
    updateDepartment: builder.mutation({ query: ({ id, body }) => ({ url: `/organization/departments/${id}`, method: 'PUT', body }), invalidatesTags: ['Department'] }),
    deleteDepartment: builder.mutation({ query: (id) => ({ url: `/organization/departments/${id}`, method: 'DELETE' }), invalidatesTags: ['Department'] }),
    getTeams: builder.query({ query: (departmentId) => `/organization/departments/${departmentId}/teams`, providesTags: ['Organization'] }),
    getHolidays: builder.query({ query: (params) => ({ url: '/organization/holidays', params: toQueryParams(params) }), providesTags: ['Organization'] }),
  }),
});

export const { useGetCompanyQuery, useGetDepartmentsQuery, useCreateDepartmentMutation, useUpdateDepartmentMutation, useDeleteDepartmentMutation, useGetTeamsQuery, useGetHolidaysQuery } = organizationApi;
