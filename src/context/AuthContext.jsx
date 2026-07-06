import React, { createContext, useContext, useMemo } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useLogoutMutation } from '../services/authApi';
import { loggedOut, selectAccessToken, selectAuthUser, selectIsAuthenticated, selectRefreshToken } from '../features/auth/authSlice';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const dispatch = useDispatch();
  const authUser = useSelector(selectAuthUser);
  const accessToken = useSelector(selectAccessToken);
  const refreshToken = useSelector(selectRefreshToken);
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const [logoutRequest] = useLogoutMutation();

  const value = useMemo(
    () => ({
      authUser,
      accessToken,
      isAuthenticated,
      login: async () => {
        throw new Error('Login is handled by RTK Query. Use useLoginMutation.');
      },
      logout: async () => {
        try {
          if (accessToken) await logoutRequest(refreshToken).unwrap();
          else dispatch(loggedOut());
        } catch {
          dispatch(loggedOut());
        }
      },
    }),
    [authUser, accessToken, isAuthenticated, refreshToken, logoutRequest, dispatch],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}

