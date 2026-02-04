type RateLimitCallback = (retryAfter: number) => void;
type HeadersCallback = (limit: number, remaining: number, reset: number) => void;

class RateLimitEventEmitter {
  private rateLimitCallbacks: RateLimitCallback[] = [];
  private headersCallbacks: HeadersCallback[] = [];

  onRateLimit(callback: RateLimitCallback): () => void {
    this.rateLimitCallbacks.push(callback);
    return () => {
      this.rateLimitCallbacks = this.rateLimitCallbacks.filter((cb) => cb !== callback);
    };
  }

  onHeadersUpdate(callback: HeadersCallback): () => void {
    this.headersCallbacks.push(callback);
    return () => {
      this.headersCallbacks = this.headersCallbacks.filter((cb) => cb !== callback);
    };
  }

  emitRateLimit(retryAfter: number): void {
    this.rateLimitCallbacks.forEach((callback) => callback(retryAfter));
  }

  emitHeadersUpdate(limit: number, remaining: number, reset: number): void {
    this.headersCallbacks.forEach((callback) => callback(limit, remaining, reset));
  }
}

export const rateLimitEvents = new RateLimitEventEmitter();
