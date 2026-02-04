import type { Artista, TipoArtista, PaginatedResponse } from './artista';

export interface CapaAlbum {
  id: number;
  minioKey: string;
  originalName: string;
  contentType: string;
  tamanhoBytes: number;
  createdAt: string;
}

export interface Album {
  id: number;
  titulo: string;
  anoLancamento: number;
  artistas: Artista[];
  capas: CapaAlbum[];
  createdAt: string;
}

export interface AlbumQueryParams {
  page?: number;
  size?: number;
  artistaId?: number;
  tipoArtista?: TipoArtista;
  direction?: 'asc' | 'desc';
}

export interface CreateAlbumRequest {
  titulo: string;
  anoLancamento: number;
  artistaIds: number[];
}

export interface UpdateAlbumRequest {
  titulo: string;
  anoLancamento: number;
  artistaIds: number[];
}

export interface CapaPresignedUrlResponse {
  url: string;
  expiresInSeconds: number;
}

export type { PaginatedResponse };
