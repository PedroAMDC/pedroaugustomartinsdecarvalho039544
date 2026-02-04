export interface NewAlbumNotificationData {
  id: number;
  titulo: string;
  artistas: string[];
}

export interface WebSocketMessage<T = unknown> {
  type: string;
  timestamp: string;
  data: T;
}

export type AlbumNotification = WebSocketMessage<NewAlbumNotificationData>;

export const MESSAGE_TYPES = {
  NEW_ALBUM: 'NEW_ALBUM',
} as const;
