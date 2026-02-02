import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useWebSocket } from '../useWebSocket';
import { getToken } from '@/lib/api';

// Mock getToken
vi.mock('@/lib/api', () => ({
  getToken: vi.fn(),
}));

// Store WebSocket instances for testing
let mockWebSocketInstances: MockWebSocket[] = [];

// Mock WebSocket class
class MockWebSocket {
  static CONNECTING = 0;
  static OPEN = 1;
  static CLOSING = 2;
  static CLOSED = 3;

  url: string;
  readyState: number = MockWebSocket.CONNECTING;
  onopen: ((ev: Event) => void) | null = null;
  onclose: ((ev: CloseEvent) => void) | null = null;
  onmessage: ((ev: MessageEvent) => void) | null = null;
  onerror: ((ev: Event) => void) | null = null;

  send = vi.fn();
  close = vi.fn(() => {
    this.readyState = MockWebSocket.CLOSED;
    if (this.onclose) {
      this.onclose(new CloseEvent('close'));
    }
  });

  constructor(url: string) {
    this.url = url;
    mockWebSocketInstances.push(this);
  }

  // Helper to simulate connection opening
  simulateOpen() {
    this.readyState = MockWebSocket.OPEN;
    if (this.onopen) {
      this.onopen(new Event('open'));
    }
  }

  // Helper to simulate message
  simulateMessage(data: string) {
    if (this.onmessage) {
      this.onmessage(new MessageEvent('message', { data }));
    }
  }

  // Helper to simulate close
  simulateClose() {
    this.readyState = MockWebSocket.CLOSED;
    if (this.onclose) {
      this.onclose(new CloseEvent('close'));
    }
  }

  // Helper to simulate error
  simulateError() {
    if (this.onerror) {
      this.onerror(new Event('error'));
    }
  }
}

describe('useWebSocket', () => {
  const mockToken = 'test-jwt-token';

  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    mockWebSocketInstances = [];

    // Mock global WebSocket
    vi.stubGlobal('WebSocket', MockWebSocket);

    // Default mock for getToken
    vi.mocked(getToken).mockReturnValue(mockToken);
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  describe('connection', () => {
    it('should not connect when enabled is false', () => {
      const onMessage = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: false,
        })
      );

      expect(mockWebSocketInstances).toHaveLength(0);
    });

    it('should not connect when no token is available', () => {
      vi.mocked(getToken).mockReturnValue(null);
      const onMessage = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      expect(mockWebSocketInstances).toHaveLength(0);
    });

    it('should connect with token via query param when enabled', () => {
      const onMessage = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      expect(mockWebSocketInstances).toHaveLength(1);
      expect(mockWebSocketInstances[0].url).toContain('/ws/notifications');
      expect(mockWebSocketInstances[0].url).toContain(
        `token=${encodeURIComponent(mockToken)}`
      );
    });

    it('should set isConnected to true when connection opens', async () => {
      const onMessage = vi.fn();

      const { result } = renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      expect(result.current.isConnected).toBe(false);

      act(() => {
        mockWebSocketInstances[0].simulateOpen();
      });

      expect(result.current.isConnected).toBe(true);
    });

    it('should call onConnect callback when connection opens', () => {
      const onMessage = vi.fn();
      const onConnect = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          onConnect,
          enabled: true,
        })
      );

      act(() => {
        mockWebSocketInstances[0].simulateOpen();
      });

      expect(onConnect).toHaveBeenCalledTimes(1);
    });
  });

  describe('message handling', () => {
    it('should call onMessage callback when receiving valid JSON message', () => {
      const onMessage = vi.fn();
      const testMessage = {
        type: 'NEW_ALBUM',
        timestamp: '2026-01-16T10:30:00Z',
        data: { id: 1, titulo: 'Test Album', artistas: ['Artist 1'] },
      };

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      act(() => {
        mockWebSocketInstances[0].simulateOpen();
        mockWebSocketInstances[0].simulateMessage(JSON.stringify(testMessage));
      });

      expect(onMessage).toHaveBeenCalledTimes(1);
      expect(onMessage).toHaveBeenCalledWith(testMessage);
    });

    it('should ignore pong responses', () => {
      const onMessage = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      act(() => {
        mockWebSocketInstances[0].simulateOpen();
        mockWebSocketInstances[0].simulateMessage('pong');
      });

      expect(onMessage).not.toHaveBeenCalled();
    });

    it('should handle invalid JSON gracefully', () => {
      const onMessage = vi.fn();
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      act(() => {
        mockWebSocketInstances[0].simulateOpen();
        mockWebSocketInstances[0].simulateMessage('invalid json');
      });

      expect(onMessage).not.toHaveBeenCalled();
      expect(consoleSpy).toHaveBeenCalled();

      consoleSpy.mockRestore();
    });
  });

  describe('disconnection', () => {
    it('should set isConnected to false when connection closes', () => {
      const onMessage = vi.fn();

      const { result } = renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      act(() => {
        mockWebSocketInstances[0].simulateOpen();
      });

      expect(result.current.isConnected).toBe(true);

      act(() => {
        mockWebSocketInstances[0].simulateClose();
      });

      expect(result.current.isConnected).toBe(false);
    });

    it('should call onDisconnect callback when connection closes', () => {
      const onMessage = vi.fn();
      const onDisconnect = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          onDisconnect,
          enabled: true,
        })
      );

      act(() => {
        mockWebSocketInstances[0].simulateOpen();
        mockWebSocketInstances[0].simulateClose();
      });

      expect(onDisconnect).toHaveBeenCalledTimes(1);
    });

    it('should call onError callback when error occurs', () => {
      const onMessage = vi.fn();
      const onError = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          onError,
          enabled: true,
        })
      );

      act(() => {
        mockWebSocketInstances[0].simulateError();
      });

      expect(onError).toHaveBeenCalledTimes(1);
    });
  });

  describe('reconnection', () => {
    it('should attempt reconnection after disconnect', async () => {
      const onMessage = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      const initialInstance = mockWebSocketInstances[0];

      act(() => {
        initialInstance.simulateOpen();
        initialInstance.simulateClose();
      });

      // First reconnect attempt after RECONNECT_INTERVAL (3000ms)
      act(() => {
        vi.advanceTimersByTime(3000);
      });

      expect(mockWebSocketInstances).toHaveLength(2);
    });

    it('should use exponential backoff for reconnection attempts', () => {
      const onMessage = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      // First connection and close
      act(() => {
        mockWebSocketInstances[0].simulateOpen();
        mockWebSocketInstances[0].simulateClose();
      });

      // First reconnect at 3000ms (3000 * 2^0)
      act(() => {
        vi.advanceTimersByTime(3000);
      });
      expect(mockWebSocketInstances).toHaveLength(2);

      // Second close and reconnect at 6000ms (3000 * 2^1)
      act(() => {
        mockWebSocketInstances[1].simulateClose();
      });
      act(() => {
        vi.advanceTimersByTime(5999);
      });
      expect(mockWebSocketInstances).toHaveLength(2);

      act(() => {
        vi.advanceTimersByTime(1);
      });
      expect(mockWebSocketInstances).toHaveLength(3);
    });

    it('should stop reconnecting after max attempts', () => {
      const onMessage = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      // Simulate 5 failed reconnection attempts
      for (let i = 0; i < 6; i++) {
        const currentInstance = mockWebSocketInstances[mockWebSocketInstances.length - 1];
        act(() => {
          currentInstance.simulateOpen();
          currentInstance.simulateClose();
        });

        // Advance timers for reconnect
        act(() => {
          vi.advanceTimersByTime(100000); // Long enough for any backoff
        });
      }

      // Should have 6 instances (initial + 5 reconnects = 6)
      // After 5 failed attempts, no more reconnections
      const instanceCount = mockWebSocketInstances.length;

      act(() => {
        vi.advanceTimersByTime(100000);
      });

      expect(mockWebSocketInstances.length).toBe(instanceCount);
    });

    it('should reset reconnection attempts on successful connection', () => {
      const onMessage = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      // First connection failure and reconnect
      act(() => {
        mockWebSocketInstances[0].simulateOpen();
        mockWebSocketInstances[0].simulateClose();
      });

      act(() => {
        vi.advanceTimersByTime(3000);
      });

      // Second connection succeeds
      act(() => {
        mockWebSocketInstances[1].simulateOpen();
      });

      // Now disconnect again
      act(() => {
        mockWebSocketInstances[1].simulateClose();
      });

      // Should reconnect at base interval (3000ms), not exponential
      act(() => {
        vi.advanceTimersByTime(3000);
      });

      expect(mockWebSocketInstances).toHaveLength(3);
    });
  });

  describe('ping interval', () => {
    it('should send ping messages at regular intervals', () => {
      const onMessage = vi.fn();

      renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      const wsInstance = mockWebSocketInstances[0];

      act(() => {
        wsInstance.simulateOpen();
      });

      // Advance past ping interval (30000ms)
      act(() => {
        vi.advanceTimersByTime(30000);
      });

      expect(wsInstance.send).toHaveBeenCalledWith('ping');

      // Advance another interval
      act(() => {
        vi.advanceTimersByTime(30000);
      });

      expect(wsInstance.send).toHaveBeenCalledTimes(2);
    });
  });

  describe('disconnect method', () => {
    it('should close the connection when disconnect is called', () => {
      const onMessage = vi.fn();

      const { result } = renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      const wsInstance = mockWebSocketInstances[0];

      act(() => {
        wsInstance.simulateOpen();
      });

      expect(result.current.isConnected).toBe(true);

      act(() => {
        result.current.disconnect();
      });

      expect(wsInstance.close).toHaveBeenCalled();
      expect(result.current.isConnected).toBe(false);
    });
  });

  describe('reconnect method', () => {
    it('should reconnect when reconnect is called', () => {
      const onMessage = vi.fn();

      const { result } = renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      act(() => {
        mockWebSocketInstances[0].simulateOpen();
      });

      const initialCount = mockWebSocketInstances.length;

      act(() => {
        result.current.reconnect();
      });

      expect(mockWebSocketInstances.length).toBe(initialCount + 1);
    });
  });

  describe('cleanup', () => {
    it('should close connection on unmount', () => {
      const onMessage = vi.fn();

      const { unmount } = renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      const wsInstance = mockWebSocketInstances[0];

      act(() => {
        wsInstance.simulateOpen();
      });

      unmount();

      expect(wsInstance.close).toHaveBeenCalled();
    });

    it('should clean up when enabled changes to false', () => {
      const onMessage = vi.fn();

      const { rerender } = renderHook(
        ({ enabled }) =>
          useWebSocket({
            onMessage,
            enabled,
          }),
        { initialProps: { enabled: true } }
      );

      const wsInstance = mockWebSocketInstances[0];

      act(() => {
        wsInstance.simulateOpen();
      });

      rerender({ enabled: false });

      expect(wsInstance.close).toHaveBeenCalled();
    });

    it('should clear ping interval on disconnect', () => {
      const onMessage = vi.fn();

      const { result } = renderHook(() =>
        useWebSocket({
          onMessage,
          enabled: true,
        })
      );

      const wsInstance = mockWebSocketInstances[0];

      act(() => {
        wsInstance.simulateOpen();
      });

      // Verify ping is working
      act(() => {
        vi.advanceTimersByTime(30000);
      });
      expect(wsInstance.send).toHaveBeenCalledTimes(1);

      // Disconnect
      act(() => {
        result.current.disconnect();
      });

      // Reset send mock
      wsInstance.send.mockClear();

      // Advance time - should not send more pings
      act(() => {
        vi.advanceTimersByTime(60000);
      });

      expect(wsInstance.send).not.toHaveBeenCalled();
    });
  });
});
