import { baseApi, toQueryParams } from './baseApi';

export const taskApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    searchTasks: builder.query({ query: (params) => ({ url: '/tasks/search', params: toQueryParams(params) }), providesTags: ['Task'] }),
    getTask: builder.query({ query: (id) => `/tasks/${id}`, providesTags: ['Task'] }),
    createTask: builder.mutation({ query: (body) => ({ url: '/tasks', method: 'POST', body }), invalidatesTags: ['Task'] }),
    updateTask: builder.mutation({ query: ({ id, body }) => ({ url: `/tasks/${id}`, method: 'PATCH', body }), invalidatesTags: ['Task'] }),
    deleteTask: builder.mutation({ query: (id) => ({ url: `/tasks/${id}`, method: 'DELETE' }), invalidatesTags: ['Task'] }),
    getTaskComments: builder.query({ query: (taskId) => `/tasks/${taskId}/comments`, providesTags: ['Task'] }),
    addTaskComment: builder.mutation({ query: ({ taskId, content }) => ({ url: `/tasks/${taskId}/comments`, method: 'POST', params: toQueryParams({ content }) }), invalidatesTags: ['Task'] }),
    getTaskActivity: builder.query({ query: ({ taskId, params }) => ({ url: `/tasks/${taskId}/activity`, params: toQueryParams(params) }), providesTags: ['Task'] }),
  }),
});

export const { useSearchTasksQuery, useGetTaskQuery, useCreateTaskMutation, useUpdateTaskMutation, useDeleteTaskMutation, useGetTaskCommentsQuery, useAddTaskCommentMutation, useGetTaskActivityQuery } = taskApi;
