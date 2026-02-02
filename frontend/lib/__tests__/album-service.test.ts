import { describe, it, expect, vi, beforeEach } from 'vitest';
import { albumService } from '../album-service';
import api from '../api';
import type {
  Album,
  CapaAlbum,
  CapaPresignedUrlResponse,
} from '@/types/album';
import type { PaginatedResponse } from '@/types/artista';

vi.mock('../api');

const mockCapa: CapaAlbum = {
  id: 1,
  minioKey: 'albuns/1/capa-123.jpg',
  originalName: 'cover.jpg',
  contentType: 'image/jpeg',
  tamanhoBytes: 102400,
  createdAt: '2025-01-01T00:00:00Z',
};

const mockAlbum: Album = {
  id: 1,
  titulo: 'Test Album',
  anoLancamento: 2024,
  artistas: [
    {
      id: 1,
      nome: 'Test Artist',
      tipo: 'CANTOR',
      quantidadeAlbuns: 1,
      createdAt: '2025-01-01T00:00:00Z',
    },
  ],
  capas: [mockCapa],
  createdAt: '2025-01-01T00:00:00Z',
};

const mockPaginatedResponse: PaginatedResponse<Album> = {
  content: [mockAlbum],
  page: 0,
  size: 12,
  totalElements: 1,
  totalPages: 1,
};

describe('AlbumService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getAll', () => {
    it('should call GET /v1/albuns without params', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: mockPaginatedResponse });

      const result = await albumService.getAll();

      expect(api.get).toHaveBeenCalledWith('/v1/albuns', { params: undefined });
      expect(result).toEqual(mockPaginatedResponse);
    });

    it('should pass query params to API', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: mockPaginatedResponse });

      const params = {
        page: 1,
        size: 24,
        tipoArtista: 'BANDA' as const,
        direction: 'desc' as const,
      };

      await albumService.getAll(params);

      expect(api.get).toHaveBeenCalledWith('/v1/albuns', { params });
    });

    it('should return paginated response', async () => {
      const multipleAlbums: PaginatedResponse<Album> = {
        content: [mockAlbum, { ...mockAlbum, id: 2, titulo: 'Album 2' }],
        page: 0,
        size: 12,
        totalElements: 2,
        totalPages: 1,
      };
      vi.mocked(api.get).mockResolvedValue({ data: multipleAlbums });

      const result = await albumService.getAll();

      expect(result.content).toHaveLength(2);
      expect(result.totalElements).toBe(2);
    });
  });

  describe('getById', () => {
    it('should call GET /v1/albuns/{id}', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: mockAlbum });

      await albumService.getById(1);

      expect(api.get).toHaveBeenCalledWith('/v1/albuns/1');
    });

    it('should return album with artistas and capas', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: mockAlbum });

      const result = await albumService.getById(1);

      expect(result.id).toBe(1);
      expect(result.titulo).toBe('Test Album');
      expect(result.artistas).toHaveLength(1);
      expect(result.capas).toHaveLength(1);
    });

    it('should propagate error on 404', async () => {
      const error = { response: { status: 404, data: { message: 'Not found' } } };
      vi.mocked(api.get).mockRejectedValue(error);

      await expect(albumService.getById(999)).rejects.toEqual(error);
    });
  });

  describe('create', () => {
    it('should call POST /v1/albuns with body', async () => {
      vi.mocked(api.post).mockResolvedValue({ data: mockAlbum });

      const createData = {
        titulo: 'New Album',
        anoLancamento: 2024,
        artistaIds: [1, 2],
      };
      await albumService.create(createData);

      expect(api.post).toHaveBeenCalledWith('/v1/albuns', createData);
    });

    it('should return created album', async () => {
      const createdAlbum = { ...mockAlbum, id: 99, titulo: 'Created Album' };
      vi.mocked(api.post).mockResolvedValue({ data: createdAlbum });

      const result = await albumService.create({
        titulo: 'Created Album',
        anoLancamento: 2024,
        artistaIds: [1],
      });

      expect(result.id).toBe(99);
      expect(result.titulo).toBe('Created Album');
    });

    it('should propagate validation error', async () => {
      const error = { response: { status: 400, data: { message: 'Titulo is required' } } };
      vi.mocked(api.post).mockRejectedValue(error);

      await expect(
        albumService.create({ titulo: '', anoLancamento: 2024, artistaIds: [] })
      ).rejects.toEqual(error);
    });
  });

  describe('update', () => {
    it('should call PUT /v1/albuns/{id} with body', async () => {
      vi.mocked(api.put).mockResolvedValue({ data: mockAlbum });

      const updateData = {
        titulo: 'Updated Album',
        anoLancamento: 2025,
        artistaIds: [1, 2, 3],
      };
      await albumService.update(1, updateData);

      expect(api.put).toHaveBeenCalledWith('/v1/albuns/1', updateData);
    });

    it('should return updated album', async () => {
      const updatedAlbum = { ...mockAlbum, titulo: 'Updated', anoLancamento: 2025 };
      vi.mocked(api.put).mockResolvedValue({ data: updatedAlbum });

      const result = await albumService.update(1, {
        titulo: 'Updated',
        anoLancamento: 2025,
        artistaIds: [1],
      });

      expect(result.titulo).toBe('Updated');
      expect(result.anoLancamento).toBe(2025);
    });

    it('should propagate error on 404', async () => {
      const error = { response: { status: 404, data: { message: 'Not found' } } };
      vi.mocked(api.put).mockRejectedValue(error);

      await expect(
        albumService.update(999, { titulo: 'Test', anoLancamento: 2024, artistaIds: [1] })
      ).rejects.toEqual(error);
    });
  });

  describe('uploadCapa', () => {
    it('should call POST /v1/albuns/{albumId}/capas with FormData', async () => {
      vi.mocked(api.post).mockResolvedValue({ data: mockCapa });

      const mockFile = new File(['test'], 'cover.jpg', { type: 'image/jpeg' });
      await albumService.uploadCapa(1, mockFile);

      expect(api.post).toHaveBeenCalledWith(
        '/v1/albuns/1/capas',
        expect.any(FormData),
        { headers: { 'Content-Type': 'multipart/form-data' } }
      );
    });

    it('should set Content-Type to multipart/form-data', async () => {
      vi.mocked(api.post).mockResolvedValue({ data: mockCapa });

      const mockFile = new File(['test'], 'cover.jpg', { type: 'image/jpeg' });
      await albumService.uploadCapa(1, mockFile);

      const callArgs = vi.mocked(api.post).mock.calls[0];
      expect(callArgs[2]).toEqual({ headers: { 'Content-Type': 'multipart/form-data' } });
    });

    it('should return created capa', async () => {
      vi.mocked(api.post).mockResolvedValue({ data: mockCapa });

      const mockFile = new File(['test'], 'cover.jpg', { type: 'image/jpeg' });
      const result = await albumService.uploadCapa(1, mockFile);

      expect(result.id).toBe(1);
      expect(result.originalName).toBe('cover.jpg');
      expect(result.contentType).toBe('image/jpeg');
    });
  });

  describe('getCapaUrl', () => {
    it('should call GET /v1/albuns/{albumId}/capas/{capaId}/url', async () => {
      const mockResponse: CapaPresignedUrlResponse = {
        url: 'https://minio.example.com/albuns/1/capa-123.jpg?signed=abc',
        expiresInSeconds: 3600,
      };
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse });

      await albumService.getCapaUrl(1, 5);

      expect(api.get).toHaveBeenCalledWith('/v1/albuns/1/capas/5/url');
    });

    it('should return presigned URL response', async () => {
      const mockResponse: CapaPresignedUrlResponse = {
        url: 'https://minio.example.com/albuns/1/capa-123.jpg?signed=abc',
        expiresInSeconds: 3600,
      };
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse });

      const result = await albumService.getCapaUrl(1, 5);

      expect(result.url).toContain('minio.example.com');
      expect(result.expiresInSeconds).toBe(3600);
    });
  });

  describe('deleteCapa', () => {
    it('should call DELETE /v1/albuns/{albumId}/capas/{capaId}', async () => {
      vi.mocked(api.delete).mockResolvedValue({});

      await albumService.deleteCapa(1, 5);

      expect(api.delete).toHaveBeenCalledWith('/v1/albuns/1/capas/5');
    });

    it('should not return data', async () => {
      vi.mocked(api.delete).mockResolvedValue({});

      const result = await albumService.deleteCapa(1, 5);

      expect(result).toBeUndefined();
    });
  });
});
