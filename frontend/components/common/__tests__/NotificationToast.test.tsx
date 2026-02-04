import { render } from '@testing-library/react';
import { NotificationProvider, showAlbumNotification } from '../NotificationToast';
import { MESSAGE_TYPES } from '@/types/notification';

vi.mock('@/hooks/useAuth', () => ({
  useAuth: () => ({ isAuthenticated: false }),
}));

vi.mock('@/hooks/useWebSocket', () => ({
  useWebSocket: vi.fn(),
}));

vi.mock('sonner', () => ({
  toast: {
    custom: vi.fn(),
    dismiss: vi.fn(),
  },
}));

describe('NotificationProvider', () => {
  it('should render children', () => {
    const { getByText } = render(
      <NotificationProvider>
        <div>Test Content</div>
      </NotificationProvider>
    );
    expect(getByText('Test Content')).toBeInTheDocument();
  });

  it('should initialize useWebSocket with onMessage handler', async () => {
    const { useWebSocket } = await import('@/hooks/useWebSocket');
    render(
      <NotificationProvider>
        <div>Content</div>
      </NotificationProvider>
    );
    expect(useWebSocket).toHaveBeenCalledWith(
      expect.objectContaining({
        onMessage: expect.any(Function),
        enabled: false,
      })
    );
  });
});

describe('showAlbumNotification', () => {
  it('should call toast.custom with notification data', async () => {
    const { toast } = await import('sonner');

    showAlbumNotification({
      id: 1,
      titulo: 'Test Album',
      artistas: ['Artist 1'],
    });

    expect(toast.custom).toHaveBeenCalledWith(
      expect.any(Function),
      expect.objectContaining({
        duration: 5000,
        position: 'top-right',
      })
    );
  });
});
