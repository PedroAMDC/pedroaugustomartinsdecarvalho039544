'use client';

import {
  createContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from 'react';
import api, { getToken, setTokens, clearTokens, getRefreshToken } from '@/lib/api';
import type { User } from '@/types/user';
import type { LoginRequest, RegisterRequest, LoginResponse } from '@/types/api';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  register: (
    nome: string,
    email: string,
    password: string,
    confirmPassword: string
  ) => Promise<void>;
  refreshToken: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | null>(null);

const decodeToken = (token: string): User | null => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return {
      id: payload.userId,
      email: payload.email || payload.upn,
      nome: payload.email?.split('@')[0] || '',
    };
  } catch {
    return null;
  }
};

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const isAuthenticated = !!user;

  useEffect(() => {
    const initializeAuth = () => {
      const token = getToken();
      if (token) {
        const decodedUser = decodeToken(token);
        if (decodedUser) {
          setUser(decodedUser);
        } else {
          clearTokens();
        }
      }
      setIsLoading(false);
    };

    initializeAuth();
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const request: LoginRequest = { email, password };
    const response = await api.post<LoginResponse>('/v1/auth/login', request);
    const { token, refreshToken } = response.data;

    setTokens(token, refreshToken);

    const decodedUser = decodeToken(token);
    setUser(decodedUser);
  }, []);

  const logout = useCallback(() => {
    clearTokens();
    setUser(null);
  }, []);

  const register = useCallback(
    async (
      nome: string,
      email: string,
      password: string,
      confirmPassword: string
    ) => {
      const request: RegisterRequest = { nome, email, password, confirmPassword };
      const response = await api.post<LoginResponse>('/v1/auth/register', request);
      const { token, refreshToken } = response.data;

      setTokens(token, refreshToken);

      const decodedUser = decodeToken(token);
      setUser(decodedUser);
    },
    []
  );

  const refreshTokenFn = useCallback(async () => {
    const currentRefreshToken = getRefreshToken();
    if (!currentRefreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await api.put<LoginResponse>('/v1/auth/refresh', {
      refreshToken: currentRefreshToken,
    });
    const { token, refreshToken: newRefreshToken } = response.data;

    setTokens(token, newRefreshToken);

    const decodedUser = decodeToken(token);
    setUser(decodedUser);
  }, []);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading,
    login,
    logout,
    register,
    refreshToken: refreshTokenFn,
  };

  return <AuthContext value={value}>{children}</AuthContext>;
}
