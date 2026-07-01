import React, { createContext, useContext, useMemo, useState } from 'react';
import { authApi, clearAuth, getStoredAuth, storeAuth } from '../lib/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const stored = getStoredAuth();
  const [authUser, setAuthUser] = useState(stored.user);
  const [accessToken, setAccessToken] = useState(stored.accessToken);

  const value = useMemo(
    () => ({
      authUser,
      accessToken,
      isAuthenticated: Boolean(accessToken),
      login: async (credentials) => {
        const data = await authApi.login(credentials);
        storeAuth(data);
        setAuthUser(data);
        setAccessToken(data.accessToken);
        return data;
      },
      logout: async () => {
        const refreshToken = getStoredAuth().refreshToken;
        try {
          if (accessToken) await authApi.logout(refreshToken);
        } catch {
          // Local logout should still complete if the backend is unavailable.
        }
        clearAuth();
        setAuthUser(null);
        setAccessToken(null);
      },
    }),
    [authUser, accessToken],
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
