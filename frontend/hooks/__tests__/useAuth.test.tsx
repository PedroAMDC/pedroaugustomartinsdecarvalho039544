import { describe, it, expect, vi, beforeEach, type Mock } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { useAuth } from '../useAuth';
import { AuthProvider } from '@/contexts/AuthContext';
import api from '@/lib/api';

// Mock do módulo api
vi.mock('@/lib/api', async () => {
  const actual = await vi.importActual<typeof import('@/lib/api')>('@/lib/api');
  return {
    ...actual,
    default: {
      post: vi.fn(),
      put: vi.fn(),
      get: vi.fn(),
    },
  };
});

// Helper para criar JWT de teste
const createMockJWT = (payload: Record<string, unknown>) => {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload));
  return `${header}.${body}.mock_signature`;
};

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key];
    }),
    clear: () => {
      store = {};
    },
  };
})();

describe('useAuth', () => {
  const mockUser = { userId: 1, email: 'test@test.com' };
  const mockToken = createMockJWT(mockUser);
  const mockRefreshToken = 'mock-refresh-token';
  const mockLoginResponse = {
    token: mockToken,
    refreshToken: mockRefreshToken,
    expiresIn: 3600,
  };

  const wrapper = ({ children }: { children: ReactNode }) => (
    <AuthProvider>{children}</AuthProvider>
  );

  beforeEach(() => {
    vi.clearAllMocks();
    Object.defineProperty(window, 'localStorage', {
      value: localStorageMock,
      writable: true,
    });
    localStorageMock.clear();
  });

  describe('estado inicial (não autenticado)', () => {
    it('should return unauthenticated state when no token exists', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });
      await waitFor(() => expect(result.current.isLoading).toBe(false));

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
    });

    it('should set isLoading to false after initialization', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });
      await waitFor(() => expect(result.current.isLoading).toBe(false));
    });
  });

  describe('login bem-sucedido', () => {
    it('should authenticate user on successful login', async () => {
      (api.post as Mock).mockResolvedValueOnce({ data: mockLoginResponse });

      const { result } = renderHook(() => useAuth(), { wrapper });
      await waitFor(() => expect(result.current.isLoading).toBe(false));

      await act(async () => {
        await result.current.login('test@test.com', 'password123');
      });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).not.toBeNull();
      expect(result.current.user?.email).toBe('test@test.com');
      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', mockToken);
      expect(localStorageMock.setItem).toHaveBeenCalledWith(
        'refreshToken',
        mockRefreshToken
      );
    });
  });

  describe('login com erro', () => {
    it('should throw error on login failure', async () => {
      (api.post as Mock).mockRejectedValueOnce(new Error('Invalid credentials'));

      const { result } = renderHook(() => useAuth(), { wrapper });
      await waitFor(() => expect(result.current.isLoading).toBe(false));

      await expect(
        act(async () => {
          await result.current.login('wrong@test.com', 'wrongpassword');
        })
      ).rejects.toThrow();

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBeNull();
    });
  });

  describe('logout', () => {
    it('should clear user and tokens on logout', async () => {
      // Setup: login first
      (api.post as Mock).mockResolvedValueOnce({ data: mockLoginResponse });

      const { result } = renderHook(() => useAuth(), { wrapper });
      await waitFor(() => expect(result.current.isLoading).toBe(false));

      await act(async () => {
        await result.current.login('test@test.com', 'password123');
      });

      expect(result.current.isAuthenticated).toBe(true);

      // Act: logout
      act(() => {
        result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token');
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('refreshToken');
    });
  });

  describe('verificação de token expirado', () => {
    it('should clear tokens if token is invalid on init', async () => {
      localStorageMock.setItem('token', 'invalid-token');

      const { result } = renderHook(() => useAuth(), { wrapper });
      await waitFor(() => expect(result.current.isLoading).toBe(false));

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(localStorageMock.removeItem).toHaveBeenCalled();
    });

    it('should restore user from valid token on init', async () => {
      localStorageMock.setItem('token', mockToken);

      const { result } = renderHook(() => useAuth(), { wrapper });
      await waitFor(() => expect(result.current.isLoading).toBe(false));

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user?.email).toBe('test@test.com');
    });
  });

  describe('refresh token', () => {
    it('should refresh tokens successfully', async () => {
      const newToken = createMockJWT({ userId: 1, email: 'test@test.com' });
      const newRefreshToken = 'new-refresh-token';

      localStorageMock.setItem('refreshToken', mockRefreshToken);
      (api.put as Mock).mockResolvedValueOnce({
        data: { token: newToken, refreshToken: newRefreshToken, expiresIn: 3600 },
      });

      const { result } = renderHook(() => useAuth(), { wrapper });
      await waitFor(() => expect(result.current.isLoading).toBe(false));

      await act(async () => {
        await result.current.refreshToken();
      });

      expect(api.put).toHaveBeenCalledWith('/v1/auth/refresh', {
        refreshToken: mockRefreshToken,
      });
      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', newToken);
      expect(result.current.isAuthenticated).toBe(true);
    });

    it('should throw error when no refresh token available', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });
      await waitFor(() => expect(result.current.isLoading).toBe(false));

      await expect(
        act(async () => {
          await result.current.refreshToken();
        })
      ).rejects.toThrow('No refresh token available');
    });
  });
});
