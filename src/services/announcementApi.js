import { baseApi, toQueryParams } from './baseApi';

export const announcementApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getAnnouncements: builder.query({ query: (params) => ({ url: '/announcements', params: toQueryParams(params) }), providesTags: ['Announcement'] }),
    createAnnouncement: builder.mutation({ query: (body) => ({ url: '/announcements', method: 'POST', body }), invalidatesTags: ['Announcement'] }),
    updateAnnouncement: builder.mutation({ query: ({ id, body }) => ({ url: `/announcements/${id}`, method: 'PUT', body }), invalidatesTags: ['Announcement'] }),
    deleteAnnouncement: builder.mutation({ query: (id) => ({ url: `/announcements/${id}`, method: 'DELETE' }), invalidatesTags: ['Announcement'] }),
  }),
});

export const { useGetAnnouncementsQuery, useCreateAnnouncementMutation, useUpdateAnnouncementMutation, useDeleteAnnouncementMutation } = announcementApi;
