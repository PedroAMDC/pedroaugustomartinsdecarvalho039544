import { tokenService } from '../token-service';

vi.mock('@/lib/api', () => ({
  default: {
    put: vi.fn(),
  },
}));

const createMockJwt = (payload: Record<string, unknown>): string => {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload));
  return `${header}.${body}.signature`;
};

describe('TokenService', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    vi.useRealTimers();
  });

  describe('getToken', () => {
    it('should return null when no token is stored', () => {
      expect(tokenService.getToken()).toBeNull();
    });

    it('should return stored token', () => {
      localStorage.setItem('token', 'test-token');
      expect(tokenService.getToken()).toBe('test-token');
    });
  });

  describe('getRefreshToken', () => {
    it('should return null when no refresh token is stored', () => {
      expect(tokenService.getRefreshToken()).toBeNull();
    });

    it('should return stored refresh token', () => {
      localStorage.setItem('refreshToken', 'test-refresh');
      expect(tokenService.getRefreshToken()).toBe('test-refresh');
    });
  });

  describe('setToken', () => {
    it('should store token in localStorage', () => {
      tokenService.setToken('new-token');
      expect(localStorage.getItem('token')).toBe('new-token');
    });

    it('should store both token and refresh token', () => {
      tokenService.setToken('new-token', 'new-refresh');
      expect(localStorage.getItem('token')).toBe('new-token');
      expect(localStorage.getItem('refreshToken')).toBe('new-refresh');
    });
  });

  describe('removeToken', () => {
    it('should remove token and refresh token from localStorage', () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('refreshToken', 'test-refresh');

      tokenService.removeToken();

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();
    });
  });

  describe('isTokenExpired', () => {
    it('should return true when no token exists', () => {
      expect(tokenService.isTokenExpired()).toBe(true);
    });

    it('should return true for expired token', () => {
      const expiredToken = createMockJwt({ exp: Math.floor(Date.now() / 1000) - 3600 });
      localStorage.setItem('token', expiredToken);

      expect(tokenService.isTokenExpired()).toBe(true);
    });

    it('should return false for valid token', () => {
      const validToken = createMockJwt({ exp: Math.floor(Date.now() / 1000) + 3600 });
      localStorage.setItem('token', validToken);

      expect(tokenService.isTokenExpired()).toBe(false);
    });

    it('should return true for malformed token', () => {
      localStorage.setItem('token', 'not-a-valid-jwt');
      expect(tokenService.isTokenExpired()).toBe(true);
    });
  });

  describe('getTokenExpiration', () => {
    it('should return null when no token exists', () => {
      expect(tokenService.getTokenExpiration()).toBeNull();
    });

    it('should return expiration in milliseconds', () => {
      const expSeconds = Math.floor(Date.now() / 1000) + 3600;
      const token = createMockJwt({ exp: expSeconds });
      localStorage.setItem('token', token);

      expect(tokenService.getTokenExpiration()).toBe(expSeconds * 1000);
    });

    it('should return null for malformed token', () => {
      localStorage.setItem('token', 'invalid');
      expect(tokenService.getTokenExpiration()).toBeNull();
    });
  });

  describe('scheduleRefresh', () => {
    it('should not schedule when no token exists', () => {
      vi.useFakeTimers();
      const callback = vi.fn();

      tokenService.scheduleRefresh(callback);

      vi.advanceTimersByTime(100000);
      expect(callback).not.toHaveBeenCalled();
    });

    it('should perform immediate refresh when token is about to expire', async () => {
      const api = await import('@/lib/api');
      const expSeconds = Math.floor(Date.now() / 1000) + 30;
      const token = createMockJwt({ exp: expSeconds });
      localStorage.setItem('token', token);
      localStorage.setItem('refreshToken', 'test-refresh');

      const newToken = createMockJwt({ exp: Math.floor(Date.now() / 1000) + 7200 });
      (api.default.put as ReturnType<typeof vi.fn>).mockResolvedValue({
        data: { token: newToken, refreshToken: 'new-refresh' },
      });

      tokenService.scheduleRefresh();

      await vi.waitFor(() => {
        expect(api.default.put).toHaveBeenCalled();
      });
    });
  });
});
