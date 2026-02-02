'use client';

import { useCallback, type ReactNode } from 'react';
import { toast } from 'sonner';
import { Disc3, X } from 'lucide-react';
import Link from 'next/link';
import { useWebSocket } from '@/hooks/useWebSocket';
import { useAuth } from '@/hooks/useAuth';
import type {
  WebSocketMessage,
  NewAlbumNotificationData,
} from '@/types/notification';
import { MESSAGE_TYPES } from '@/types/notification';

interface NotificationProviderProps {
  children: ReactNode;
}

function AlbumNotificationContent({
  data,
  toastId,
}: {
  data: NewAlbumNotificationData;
  toastId: string | number;
}) {
  return (
    <div className="flex w-full items-start gap-3 rounded-lg border bg-card p-4 shadow-lg">
      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10">
        <Disc3 className="h-5 w-5 text-primary" />
      </div>
      <div className="flex-1 space-y-1">
        <p className="text-sm font-medium">Novo album cadastrado!</p>
        <p className="text-sm text-muted-foreground">{data.titulo}</p>
        {data.artistas.length > 0 && (
          <p className="text-xs text-muted-foreground">
            {data.artistas.join(', ')}
          </p>
        )}
        <Link
          href={`/albuns/${data.id}/editar`}
          className="mt-2 inline-block text-sm font-medium text-primary hover:underline"
          onClick={() => toast.dismiss(toastId)}
        >
          Ver album
        </Link>
      </div>
      <button
        type="button"
        onClick={() => toast.dismiss(toastId)}
        className="shrink-0 rounded-sm opacity-70 transition-opacity hover:opacity-100"
        aria-label="Fechar notificacao"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  );
}

export function showAlbumNotification(data: NewAlbumNotificationData) {
  toast.custom(
    (id) => <AlbumNotificationContent data={data} toastId={id} />,
    {
      duration: 5000,
      position: 'top-right',
    }
  );
}

export function NotificationProvider({ children }: NotificationProviderProps) {
  const { isAuthenticated } = useAuth();

  const handleMessage = useCallback((message: WebSocketMessage) => {
    if (message.type === MESSAGE_TYPES.NEW_ALBUM) {
      const data = message.data as NewAlbumNotificationData;
      showAlbumNotification(data);
    }
  }, []);

  useWebSocket({
    onMessage: handleMessage,
    enabled: isAuthenticated,
  });

  return <>{children}</>;
}
