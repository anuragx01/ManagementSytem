import { baseApi } from './baseApi';
import { credentialsReceived, loggedOut } from '../features/auth/authSlice';

export const authApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    login: builder.mutation({
      query: ({ email, password }) => ({ url: '/auth/login', method: 'POST', body: { email, password } }),
      invalidatesTags: ['Auth', 'Profile'],
      async onQueryStarted(_arg, { dispatch, queryFulfilled }) {
        const { data } = await queryFulfilled;
        dispatch(credentialsReceived(data));
      },
    }),
    logout: builder.mutation({
      query: (refreshToken) => ({ url: '/auth/logout', method: 'POST', body: refreshToken ? { refreshToken } : {} }),
      async onQueryStarted(_arg, { dispatch, queryFulfilled }) {
        try { await queryFulfilled; } catch { /* local logout still completes */ }
        dispatch(loggedOut());
        dispatch(baseApi.util.resetApiState());
      },
    }),
    changePassword: builder.mutation({
      query: (body) => ({ url: '/auth/change-password', method: 'POST', body }),
    }),
  }),
});

export const { useLoginMutation, useLogoutMutation, useChangePasswordMutation } = authApi;
