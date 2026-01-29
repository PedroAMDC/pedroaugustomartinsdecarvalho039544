export type TipoArtista = 'CANTOR' | 'BANDA';

export interface Artista {
  id: number;
  nome: string;
  tipo: TipoArtista;
  quantidadeAlbuns: number;
  createdAt: string;
}

export interface AlbumSummary {
  id: number;
  titulo: string;
  anoLancamento: number;
}

export interface ArtistaDetail extends Omit<Artista, 'quantidadeAlbuns'> {
  albuns: AlbumSummary[];
}

export interface ArtistaQueryParams {
  page?: number;
  size?: number;
  nome?: string;
  tipo?: TipoArtista;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreateArtistaRequest {
  nome: string;
  tipo: TipoArtista;
}

export interface UpdateArtistaRequest {
  nome: string;
  tipo: TipoArtista;
}
