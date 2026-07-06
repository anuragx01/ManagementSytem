import { baseApi, toQueryParams } from './baseApi';

export const notificationApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getNotifications: builder.query({ query: (params) => ({ url: '/notifications', params: toQueryParams(params) }), providesTags: ['Notification'] }),
    getUnreadNotifications: builder.query({ query: (params) => ({ url: '/notifications/unread', params: toQueryParams(params) }), providesTags: ['Notification'] }),
    getUnreadCount: builder.query({ query: () => '/notifications/unread/count', providesTags: ['Notification'] }),
    markNotificationRead: builder.mutation({ query: (id) => ({ url: `/notifications/${id}/read`, method: 'PATCH' }), invalidatesTags: ['Notification'] }),
    markAllNotificationsRead: builder.mutation({ query: () => ({ url: '/notifications/read-all', method: 'POST' }), invalidatesTags: ['Notification'] }),
    archiveNotification: builder.mutation({ query: (id) => ({ url: `/notifications/${id}/archive`, method: 'PATCH' }), invalidatesTags: ['Notification'] }),
    deleteNotification: builder.mutation({ query: (id) => ({ url: `/notifications/${id}`, method: 'DELETE' }), invalidatesTags: ['Notification'] }),
  }),
});

export const { useGetNotificationsQuery, useGetUnreadNotificationsQuery, useGetUnreadCountQuery, useMarkNotificationReadMutation, useMarkAllNotificationsReadMutation, useArchiveNotificationMutation, useDeleteNotificationMutation } = notificationApi;
