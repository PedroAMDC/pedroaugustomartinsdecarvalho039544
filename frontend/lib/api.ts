import axios, { AxiosError, AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import type { LoginResponse, ApiError } from '@/types/api';
import { rateLimitEvents } from './rate-limit-events';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const api: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

const TOKEN_KEY = 'token';
const REFRESH_TOKEN_KEY = 'refreshToken';

export const getToken = (): string | null => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
};

export const getRefreshToken = (): string | null => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(REFRESH_TOKEN_KEY);
};

export const setTokens = (token: string, refreshToken: string): void => {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  document.cookie = `token=${token}; path=/; max-age=${60 * 60 * 24}`;
};

export const clearTokens = (): void => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  document.cookie = 'token=; path=/; max-age=0';
};

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null): void => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else if (token) {
      promise.resolve(token);
    }
  });
  failedQueue = [];
};

const extractRateLimitHeaders = (response: AxiosResponse): void => {
  const limit = response.headers['x-ratelimit-limit'];
  const remaining = response.headers['x-ratelimit-remaining'];
  const reset = response.headers['x-ratelimit-reset'];

  if (limit && remaining && reset) {
    rateLimitEvents.emitHeadersUpdate(
      parseInt(limit, 10),
      parseInt(remaining, 10),
      parseInt(reset, 10)
    );
  }
};

api.interceptors.response.use(
  (response) => {
    extractRateLimitHeaders(response);
    return response;
  },
  async (error: AxiosError<ApiError>) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && originalRequest) {
      const isAuthEndpoint =
        originalRequest.url?.includes('/auth/login') ||
        originalRequest.url?.includes('/auth/register');

      if (isAuthEndpoint) {
        return Promise.reject(error);
      }

      const refreshToken = getRefreshToken();

      if (
        !refreshToken ||
        (originalRequest as InternalAxiosRequestConfig & { _retry?: boolean })._retry
      ) {
        clearTokens();
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      (originalRequest as InternalAxiosRequestConfig & { _retry?: boolean })._retry = true;
      isRefreshing = true;

      try {
        const response = await axios.put<LoginResponse>(
          `${API_URL}/v1/auth/refresh`,
          { refreshToken },
          { headers: { 'Content-Type': 'application/json' } }
        );

        const { token, refreshToken: newRefreshToken } = response.data;
        setTokens(token, newRefreshToken);

        processQueue(null, token);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${token}`;
        }
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        clearTokens();
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    if (error.response?.status === 429) {
      const retryAfterHeader = error.response.headers['retry-after'];
      const retryAfterBody = (error.response.data as { retryAfter?: number })?.retryAfter;
      const retryAfter = parseInt(retryAfterHeader, 10) || retryAfterBody || 60;

      rateLimitEvents.emitRateLimit(retryAfter);

      return Promise.reject(error);
    }

    return Promise.reject(error);
  }
);

export default api;
