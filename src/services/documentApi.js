import { baseApi, toQueryParams } from './baseApi';

export const documentApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    uploadProfilePicture: builder.mutation({
      query: (file) => {
        const formData = new FormData();
        formData.append('file', file);
        return { url: '/documents/profile-picture', method: 'POST', body: formData };
      },
      invalidatesTags: ['Profile', 'Document'],
    }),
    uploadTaskAttachment: builder.mutation({
      query: ({ taskId, file }) => {
        const formData = new FormData();
        formData.append('file', file);
        return { url: `/documents/task/${taskId}/upload`, method: 'POST', body: formData };
      },
      invalidatesTags: ['Task', 'Document'],
    }),
    getPresignedUrl: builder.query({ query: (key) => ({ url: '/documents/presigned-url', params: toQueryParams({ key }) }), providesTags: ['Document'] }),
  }),
});

export const { useUploadProfilePictureMutation, useUploadTaskAttachmentMutation, useGetPresignedUrlQuery } = documentApi;

