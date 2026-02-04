import { rateLimitEvents } from '../rate-limit-events';

describe('RateLimitEventEmitter', () => {
  describe('onRateLimit', () => {
    it('should register and call rate limit callback', () => {
      const callback = vi.fn();
      rateLimitEvents.onRateLimit(callback);

      rateLimitEvents.emitRateLimit(60);

      expect(callback).toHaveBeenCalledWith(60);
    });

    it('should support multiple callbacks', () => {
      const callback1 = vi.fn();
      const callback2 = vi.fn();
      rateLimitEvents.onRateLimit(callback1);
      rateLimitEvents.onRateLimit(callback2);

      rateLimitEvents.emitRateLimit(30);

      expect(callback1).toHaveBeenCalledWith(30);
      expect(callback2).toHaveBeenCalledWith(30);
    });

    it('should unsubscribe when calling returned function', () => {
      const callback = vi.fn();
      const unsubscribe = rateLimitEvents.onRateLimit(callback);

      unsubscribe();
      rateLimitEvents.emitRateLimit(60);

      expect(callback).not.toHaveBeenCalled();
    });
  });

  describe('onHeadersUpdate', () => {
    it('should register and call headers update callback', () => {
      const callback = vi.fn();
      rateLimitEvents.onHeadersUpdate(callback);

      rateLimitEvents.emitHeadersUpdate(100, 50, 1700000000);

      expect(callback).toHaveBeenCalledWith(100, 50, 1700000000);
    });

    it('should unsubscribe when calling returned function', () => {
      const callback = vi.fn();
      const unsubscribe = rateLimitEvents.onHeadersUpdate(callback);

      unsubscribe();
      rateLimitEvents.emitHeadersUpdate(100, 50, 1700000000);

      expect(callback).not.toHaveBeenCalled();
    });
  });
});
