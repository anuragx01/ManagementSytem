import { baseApi } from './baseApi';

export const dailyReportApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getMyDailyReports: builder.query({ query: () => '/daily-reports/my', providesTags: ['DailyReport'] }),
    getDailyReports: builder.query({ query: () => '/daily-reports', providesTags: ['DailyReport'] }),
    submitMyDailyReport: builder.mutation({ query: (body) => ({ url: '/daily-reports/my', method: 'POST', body }), invalidatesTags: ['DailyReport'] }),
  }),
});

export const { useGetMyDailyReportsQuery, useGetDailyReportsQuery, useSubmitMyDailyReportMutation } = dailyReportApi;
