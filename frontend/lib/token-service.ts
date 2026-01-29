import api from '@/lib/api';
import type { LoginResponse } from '@/types/api';

class TokenService {
  private readonly TOKEN_KEY = 'token';
  private readonly REFRESH_TOKEN_KEY = 'refreshToken';
  private refreshTimeout: ReturnType<typeof setTimeout> | null = null;

  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  setToken(token: string, refreshToken?: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    if (refreshToken) {
      localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
    }
  }

  removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    this.cancelScheduledRefresh();
  }

  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;

    try {
      const expiration = this.getTokenExpiration();
      if (!expiration) return true;
      return expiration < Date.now();
    } catch {
      return true;
    }
  }

  getTokenExpiration(): number | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000;
    } catch {
      return null;
    }
  }

  scheduleRefresh(onRefresh?: (token: string) => void): void {
    this.cancelScheduledRefresh();

    const expiration = this.getTokenExpiration();
    if (!expiration) return;

    const now = Date.now();
    const timeUntilExpiry = expiration - now;
    const refreshTime = timeUntilExpiry - 60000;

    if (refreshTime <= 0) {
      this.performRefresh(onRefresh);
      return;
    }

    this.refreshTimeout = setTimeout(() => {
      this.performRefresh(onRefresh);
    }, refreshTime);
  }

  private cancelScheduledRefresh(): void {
    if (this.refreshTimeout) {
      clearTimeout(this.refreshTimeout);
      this.refreshTimeout = null;
    }
  }

  private async performRefresh(
    onRefresh?: (token: string) => void
  ): Promise<void> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) return;

    try {
      const { data } = await api.put<LoginResponse>('/v1/auth/refresh', {
        refreshToken,
      });

      this.setToken(data.token, data.refreshToken);
      this.scheduleRefresh(onRefresh);

      if (onRefresh) {
        onRefresh(data.token);
      }
    } catch {
      this.removeToken();
    }
  }
}

export const tokenService = new TokenService();
