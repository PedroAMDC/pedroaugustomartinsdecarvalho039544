'use client';

import { createContext, useState, useCallback, useContext, useEffect, type ReactNode } from 'react';
import { RateLimitError } from '@/components/errors/RateLimitError';
import { rateLimitEvents } from '@/lib/rate-limit-events';

interface RateLimitState {
  isLimited: boolean;
  retryAfter: number;
  limitReached: number;
  remaining: number;
  resetAt: number | null;
}

interface RateLimitContextType {
  state: RateLimitState;
  triggerRateLimit: (retryAfter: number) => void;
  clearRateLimit: () => void;
  updateHeaders: (limit: number, remaining: number, reset: number) => void;
}

const initialState: RateLimitState = {
  isLimited: false,
  retryAfter: 0,
  limitReached: 10,
  remaining: 10,
  resetAt: null,
};

export const RateLimitContext = createContext<RateLimitContextType | null>(null);

interface RateLimitProviderProps {
  children: ReactNode;
}

export function RateLimitProvider({ children }: RateLimitProviderProps) {
  const [state, setState] = useState<RateLimitState>(initialState);

  const triggerRateLimit = useCallback((retryAfter: number) => {
    setState((prev) => ({
      ...prev,
      isLimited: true,
      retryAfter,
      remaining: 0,
      resetAt: Date.now() + retryAfter * 1000,
    }));
  }, []);

  const clearRateLimit = useCallback(() => {
    setState((prev) => ({
      ...prev,
      isLimited: false,
      retryAfter: 0,
      remaining: prev.limitReached,
      resetAt: null,
    }));
  }, []);

  const updateHeaders = useCallback((limit: number, remaining: number, reset: number) => {
    setState((prev) => ({
      ...prev,
      limitReached: limit,
      remaining,
      resetAt: reset * 1000,
    }));
  }, []);

  useEffect(() => {
    const unsubscribeRateLimit = rateLimitEvents.onRateLimit(triggerRateLimit);
    const unsubscribeHeaders = rateLimitEvents.onHeadersUpdate(updateHeaders);

    return () => {
      unsubscribeRateLimit();
      unsubscribeHeaders();
    };
  }, [triggerRateLimit, updateHeaders]);

  const handleRetry = useCallback(() => {
    clearRateLimit();
    window.location.reload();
  }, [clearRateLimit]);

  const handleDismiss = useCallback(() => {
    clearRateLimit();
  }, [clearRateLimit]);

  const value: RateLimitContextType = {
    state,
    triggerRateLimit,
    clearRateLimit,
    updateHeaders,
  };

  return (
    <RateLimitContext value={value}>
      {children}
      {state.isLimited && (
        <RateLimitError
          retryAfter={state.retryAfter}
          onRetry={handleRetry}
          onDismiss={handleDismiss}
        />
      )}
    </RateLimitContext>
  );
}

export function useRateLimit() {
  const context = useContext(RateLimitContext);
  if (!context) {
    throw new Error('useRateLimit must be used within a RateLimitProvider');
  }
  return context;
}

export function useRateLimitState() {
  const { state } = useRateLimit();
  return state;
}
