import { baseApi, toQueryParams } from './baseApi';

export const projectApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getProjects: builder.query({ query: (params) => ({ url: '/projects', params: toQueryParams(params) }), providesTags: ['Project'] }),
    getProject: builder.query({ query: (id) => `/projects/${id}`, providesTags: ['Project'] }),
    createProject: builder.mutation({ query: (body) => ({ url: '/projects', method: 'POST', body }), invalidatesTags: ['Project'] }),
    updateProject: builder.mutation({ query: ({ id, body }) => ({ url: `/projects/${id}`, method: 'PUT', body }), invalidatesTags: ['Project'] }),
    getProjectMembers: builder.query({ query: (projectId) => `/projects/${projectId}/members`, providesTags: ['Project'] }),
  }),
});

export const { useGetProjectsQuery, useGetProjectQuery, useCreateProjectMutation, useUpdateProjectMutation, useGetProjectMembersQuery } = projectApi;
