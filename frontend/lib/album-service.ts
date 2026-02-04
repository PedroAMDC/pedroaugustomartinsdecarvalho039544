import api from '@/lib/api';
import type {
  Album,
  AlbumQueryParams,
  CreateAlbumRequest,
  UpdateAlbumRequest,
  CapaAlbum,
  CapaPresignedUrlResponse,
} from '@/types/album';
import type { PaginatedResponse } from '@/types/artista';

class AlbumService {
  async getAll(params?: AlbumQueryParams): Promise<PaginatedResponse<Album>> {
    const { data } = await api.get<PaginatedResponse<Album>>('/v1/albuns', {
      params,
    });
    return data;
  }

  async getById(id: number): Promise<Album> {
    const { data } = await api.get<Album>(`/v1/albuns/${id}`);
    return data;
  }

  async create(album: CreateAlbumRequest): Promise<Album> {
    const { data } = await api.post<Album>('/v1/albuns', album);
    return data;
  }

  async update(id: number, album: UpdateAlbumRequest): Promise<Album> {
    const { data } = await api.put<Album>(`/v1/albuns/${id}`, album);
    return data;
  }

  async uploadCapa(
    albumId: number,
    file: File,
    onProgress?: (percent: number) => void
  ): Promise<CapaAlbum> {
    const formData = new FormData();
    formData.append('file', file);

    const { data } = await api.post<CapaAlbum>(`/v1/albuns/${albumId}/capas`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percent);
        }
      },
    });
    return data;
  }

  async getCapaUrl(albumId: number, capaId: number): Promise<CapaPresignedUrlResponse> {
    const { data } = await api.get<CapaPresignedUrlResponse>(
      `/v1/albuns/${albumId}/capas/${capaId}/url`
    );
    return data;
  }

  async deleteCapa(albumId: number, capaId: number): Promise<void> {
    await api.delete(`/v1/albuns/${albumId}/capas/${capaId}`);
  }
}

export const albumService = new AlbumService();
