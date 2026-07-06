import { createSlice } from '@reduxjs/toolkit';

const TOKEN_KEY = 'nexstar-access-token';
const REFRESH_TOKEN_KEY = 'nexstar-refresh-token';
const USER_KEY = 'nexstar-auth-user';

function readUser() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

const initialState = {
  user: readUser(),
  accessToken: localStorage.getItem(TOKEN_KEY),
  refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY),
};

function persistAuth(authData) {
  if (authData?.accessToken) localStorage.setItem(TOKEN_KEY, authData.accessToken);
  if (authData?.refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, authData.refreshToken);
  localStorage.setItem(USER_KEY, JSON.stringify(authData));
}

function clearPersistedAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    credentialsReceived(state, action) {
      const authData = action.payload || {};
      state.user = authData;
      state.accessToken = authData.accessToken || state.accessToken;
      state.refreshToken = authData.refreshToken || state.refreshToken;
      persistAuth({ ...authData, accessToken: state.accessToken, refreshToken: state.refreshToken });
    },
    loggedOut(state) {
      state.user = null;
      state.accessToken = null;
      state.refreshToken = null;
      clearPersistedAuth();
    },
  },
});

export const { credentialsReceived, loggedOut } = authSlice.actions;
export const selectAuthUser = (state) => state.auth.user;
export const selectAccessToken = (state) => state.auth.accessToken;
export const selectRefreshToken = (state) => state.auth.refreshToken;
export const selectIsAuthenticated = (state) => Boolean(state.auth.accessToken);
export default authSlice.reducer;

