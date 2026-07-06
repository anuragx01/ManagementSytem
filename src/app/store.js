import { configureStore } from '@reduxjs/toolkit';
import { baseApi } from '../services/baseApi';
import authReducer from '../features/auth/authSlice';
import '../services/authApi';
import '../services/employeeApi';
import '../services/organizationApi';
import '../services/notificationApi';
import '../services/attendanceApi';
import '../services/leaveApi';
import '../services/taskApi';
import '../services/projectApi';
import '../services/documentApi';
import '../services/payrollApi';
import '../services/reportApi';
import '../services/announcementApi';
import '../services/dailyReportApi';
import '../services/adminApi';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [baseApi.reducerPath]: baseApi.reducer,
  },
  middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(baseApi.middleware),
});
