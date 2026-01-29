import api from '@/lib/api';
import type {
  Artista,
  ArtistaDetail,
  ArtistaQueryParams,
  PaginatedResponse,
  CreateArtistaRequest,
  UpdateArtistaRequest,
} from '@/types/artista';

class ArtistaService {
  async getAll(
    params?: ArtistaQueryParams
  ): Promise<PaginatedResponse<Artista>> {
    const { data } = await api.get<PaginatedResponse<Artista>>(
      '/v1/artistas',
      { params }
    );
    return data;
  }

  async getById(id: number): Promise<ArtistaDetail> {
    const { data } = await api.get<ArtistaDetail>(`/v1/artistas/${id}`);
    return data;
  }

  async create(artista: CreateArtistaRequest): Promise<Artista> {
    const { data } = await api.post<Artista>('/v1/artistas', artista);
    return data;
  }

  async update(id: number, artista: UpdateArtistaRequest): Promise<Artista> {
    const { data } = await api.put<Artista>(`/v1/artistas/${id}`, artista);
    return data;
  }
}

export const artistaService = new ArtistaService();
