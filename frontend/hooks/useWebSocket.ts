'use client';

import { useEffect, useRef, useCallback, useState } from 'react';
import { getToken } from '@/lib/api';
import type { WebSocketMessage } from '@/types/notification';

const WS_URL = process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080';
const RECONNECT_INTERVAL = 3000;
const MAX_RECONNECT_ATTEMPTS = 5;
const PING_INTERVAL = 30000;

interface UseWebSocketOptions {
  onMessage: (message: WebSocketMessage) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: Event) => void;
  enabled?: boolean;
}

export function useWebSocket({
  onMessage,
  onConnect,
  onDisconnect,
  onError,
  enabled = true,
}: UseWebSocketOptions) {
  const [isConnected, setIsConnected] = useState(false);
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectAttemptsRef = useRef(0);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const pingIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const connectRef = useRef<() => void>(() => {});

  const handlersRef = useRef({ onMessage, onConnect, onDisconnect, onError });

  useEffect(() => {
    handlersRef.current = { onMessage, onConnect, onDisconnect, onError };
  }, [onMessage, onConnect, onDisconnect, onError]);

  const cleanup = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }
    if (pingIntervalRef.current) {
      clearInterval(pingIntervalRef.current);
      pingIntervalRef.current = null;
    }
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
    setIsConnected(false);
  }, []);

  useEffect(() => {
    const createConnection = () => {
      const token = getToken();

      if (!token || !enabled) {
        return;
      }

      cleanup();

      const wsUrl = `${WS_URL}/ws/notifications?token=${encodeURIComponent(token)}`;
      const ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        reconnectAttemptsRef.current = 0;
        setIsConnected(true);
        handlersRef.current.onConnect?.();

        pingIntervalRef.current = setInterval(() => {
          if (ws.readyState === WebSocket.OPEN) {
            ws.send('ping');
          }
        }, PING_INTERVAL);
      };

      ws.onmessage = (event) => {
        const data = event.data;

        if (data === 'pong') {
          return;
        }

        try {
          const message = JSON.parse(data) as WebSocketMessage;
          handlersRef.current.onMessage(message);
        } catch {}
      };

      ws.onclose = () => {
        if (pingIntervalRef.current) {
          clearInterval(pingIntervalRef.current);
          pingIntervalRef.current = null;
        }

        setIsConnected(false);
        handlersRef.current.onDisconnect?.();

        if (enabled && reconnectAttemptsRef.current < MAX_RECONNECT_ATTEMPTS) {
          const delay = RECONNECT_INTERVAL * Math.pow(2, reconnectAttemptsRef.current);
          reconnectAttemptsRef.current += 1;

          reconnectTimeoutRef.current = setTimeout(() => {
            connectRef.current();
          }, delay);
        }
      };

      ws.onerror = (error) => {
        handlersRef.current.onError?.(error);
      };

      wsRef.current = ws;
    };

    connectRef.current = createConnection;

    if (enabled) {
      createConnection();
    }

    return cleanup;
  }, [enabled, cleanup]);

  const disconnect = useCallback(() => {
    cleanup();
  }, [cleanup]);

  const reconnect = useCallback(() => {
    reconnectAttemptsRef.current = 0;
    connectRef.current();
  }, []);

  return {
    disconnect,
    reconnect,
    isConnected,
  };
}
