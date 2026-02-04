import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RateLimitProvider, useRateLimit, useRateLimitState } from '../RateLimitContext';
import { rateLimitEvents } from '@/lib/rate-limit-events';

vi.mock('@/components/errors/RateLimitError', () => ({
  RateLimitError: ({ retryAfter, onRetry, onDismiss }: {
    retryAfter: number;
    onRetry: () => void;
    onDismiss: () => void;
  }) => (
    <div data-testid="rate-limit-error">
      <span>Rate limited: {retryAfter}s</span>
      <button onClick={onRetry}>Retry</button>
      <button onClick={onDismiss}>Dismiss</button>
    </div>
  ),
}));

function TestConsumer() {
  const { state, triggerRateLimit, clearRateLimit, updateHeaders } = useRateLimit();
  return (
    <div>
      <span data-testid="is-limited">{String(state.isLimited)}</span>
      <span data-testid="remaining">{state.remaining}</span>
      <span data-testid="retry-after">{state.retryAfter}</span>
      <button onClick={() => triggerRateLimit(60)}>Trigger</button>
      <button onClick={() => clearRateLimit()}>Clear</button>
      <button onClick={() => updateHeaders(100, 50, 1700000000)}>Update</button>
    </div>
  );
}

function TestStateConsumer() {
  const state = useRateLimitState();
  return <span data-testid="state-limited">{String(state.isLimited)}</span>;
}

describe('RateLimitProvider', () => {
  it('should render children', () => {
    render(
      <RateLimitProvider>
        <div>Content</div>
      </RateLimitProvider>
    );
    expect(screen.getByText('Content')).toBeInTheDocument();
  });

  it('should provide initial state', () => {
    render(
      <RateLimitProvider>
        <TestConsumer />
      </RateLimitProvider>
    );
    expect(screen.getByTestId('is-limited')).toHaveTextContent('false');
    expect(screen.getByTestId('remaining')).toHaveTextContent('10');
  });

  it('should trigger rate limit', async () => {
    const user = userEvent.setup();
    render(
      <RateLimitProvider>
        <TestConsumer />
      </RateLimitProvider>
    );

    await user.click(screen.getByText('Trigger'));

    expect(screen.getByTestId('is-limited')).toHaveTextContent('true');
    expect(screen.getByTestId('retry-after')).toHaveTextContent('60');
    expect(screen.getByTestId('rate-limit-error')).toBeInTheDocument();
  });

  it('should clear rate limit', async () => {
    const user = userEvent.setup();
    render(
      <RateLimitProvider>
        <TestConsumer />
      </RateLimitProvider>
    );

    await user.click(screen.getByText('Trigger'));
    expect(screen.getByTestId('is-limited')).toHaveTextContent('true');

    await user.click(screen.getByText('Clear'));
    expect(screen.getByTestId('is-limited')).toHaveTextContent('false');
  });

  it('should update headers', async () => {
    const user = userEvent.setup();
    render(
      <RateLimitProvider>
        <TestConsumer />
      </RateLimitProvider>
    );

    await user.click(screen.getByText('Update'));
    expect(screen.getByTestId('remaining')).toHaveTextContent('50');
  });

  it('should dismiss rate limit error', async () => {
    const user = userEvent.setup();
    render(
      <RateLimitProvider>
        <TestConsumer />
      </RateLimitProvider>
    );

    await user.click(screen.getByText('Trigger'));
    expect(screen.getByTestId('rate-limit-error')).toBeInTheDocument();

    await user.click(screen.getByText('Dismiss'));
    expect(screen.queryByTestId('rate-limit-error')).not.toBeInTheDocument();
  });

  it('should react to rateLimitEvents', () => {
    render(
      <RateLimitProvider>
        <TestConsumer />
      </RateLimitProvider>
    );

    act(() => {
      rateLimitEvents.emitRateLimit(30);
    });

    expect(screen.getByTestId('is-limited')).toHaveTextContent('true');
    expect(screen.getByTestId('retry-after')).toHaveTextContent('30');
  });

  it('should react to header update events', () => {
    render(
      <RateLimitProvider>
        <TestConsumer />
      </RateLimitProvider>
    );

    act(() => {
      rateLimitEvents.emitHeadersUpdate(100, 25, 1700000000);
    });

    expect(screen.getByTestId('remaining')).toHaveTextContent('25');
  });
});

describe('useRateLimit', () => {
  it('should throw when used outside provider', () => {
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});

    expect(() => render(<TestConsumer />)).toThrow(
      'useRateLimit must be used within a RateLimitProvider'
    );

    spy.mockRestore();
  });
});

describe('useRateLimitState', () => {
  it('should return state from context', () => {
    render(
      <RateLimitProvider>
        <TestStateConsumer />
      </RateLimitProvider>
    );
    expect(screen.getByTestId('state-limited')).toHaveTextContent('false');
  });
});
