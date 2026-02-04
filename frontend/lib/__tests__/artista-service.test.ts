import { describe, it, expect, vi, beforeEach } from 'vitest';
import { artistaService } from '../artista-service';
import api from '../api';
import type {
  Artista,
  ArtistaDetail,
  PaginatedResponse,
} from '@/types/artista';

vi.mock('../api');

const mockArtista: Artista = {
  id: 1,
  nome: 'Test Artist',
  tipo: 'CANTOR',
  quantidadeAlbuns: 3,
  createdAt: '2025-01-01T00:00:00Z',
};

const mockArtistaDetail: ArtistaDetail = {
  id: 1,
  nome: 'Test Artist',
  tipo: 'CANTOR',
  createdAt: '2025-01-01T00:00:00Z',
  albuns: [
    { id: 1, titulo: 'Album 1', anoLancamento: 2020, capaUrl: null },
    { id: 2, titulo: 'Album 2', anoLancamento: 2021, capaUrl: null },
  ],
};

const mockPaginatedResponse: PaginatedResponse<Artista> = {
  content: [mockArtista],
  page: 0,
  size: 10,
  totalElements: 1,
  totalPages: 1,
};

describe('ArtistaService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getAll', () => {
    it('should call GET /v1/artistas without params', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: mockPaginatedResponse });

      const result = await artistaService.getAll();

      expect(api.get).toHaveBeenCalledWith('/v1/artistas', { params: undefined });
      expect(result).toEqual(mockPaginatedResponse);
    });

    it('should pass query params to API', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: mockPaginatedResponse });

      const params = {
        page: 1,
        size: 20,
        nome: 'Test',
        tipo: 'BANDA' as const,
        sort: 'nome',
        direction: 'desc' as const,
      };

      await artistaService.getAll(params);

      expect(api.get).toHaveBeenCalledWith('/v1/artistas', { params });
    });

    it('should return paginated response', async () => {
      const multipleArtists: PaginatedResponse<Artista> = {
        content: [mockArtista, { ...mockArtista, id: 2, nome: 'Artist 2' }],
        page: 0,
        size: 10,
        totalElements: 2,
        totalPages: 1,
      };
      vi.mocked(api.get).mockResolvedValue({ data: multipleArtists });

      const result = await artistaService.getAll();

      expect(result.content).toHaveLength(2);
      expect(result.totalElements).toBe(2);
    });
  });

  describe('getById', () => {
    it('should call GET /v1/artistas/{id}', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: mockArtistaDetail });

      await artistaService.getById(1);

      expect(api.get).toHaveBeenCalledWith('/v1/artistas/1');
    });

    it('should return artista detail with albums', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: mockArtistaDetail });

      const result = await artistaService.getById(1);

      expect(result.id).toBe(1);
      expect(result.nome).toBe('Test Artist');
      expect(result.albuns).toHaveLength(2);
    });

    it('should propagate error on 404', async () => {
      const error = { response: { status: 404, data: { message: 'Not found' } } };
      vi.mocked(api.get).mockRejectedValue(error);

      await expect(artistaService.getById(999)).rejects.toEqual(error);
    });
  });

  describe('create', () => {
    it('should call POST /v1/artistas with body', async () => {
      vi.mocked(api.post).mockResolvedValue({ data: mockArtista });

      const createData = { nome: 'New Artist', tipo: 'CANTOR' as const };
      await artistaService.create(createData);

      expect(api.post).toHaveBeenCalledWith('/v1/artistas', createData);
    });

    it('should return created artista', async () => {
      const createdArtista = { ...mockArtista, id: 99, nome: 'Created' };
      vi.mocked(api.post).mockResolvedValue({ data: createdArtista });

      const result = await artistaService.create({ nome: 'Created', tipo: 'BANDA' });

      expect(result.id).toBe(99);
      expect(result.nome).toBe('Created');
    });

    it('should propagate validation error', async () => {
      const error = { response: { status: 400, data: { message: 'Nome is required' } } };
      vi.mocked(api.post).mockRejectedValue(error);

      await expect(artistaService.create({ nome: '', tipo: 'CANTOR' })).rejects.toEqual(error);
    });
  });

  describe('update', () => {
    it('should call PUT /v1/artistas/{id} with body', async () => {
      vi.mocked(api.put).mockResolvedValue({ data: mockArtista });

      const updateData = { nome: 'Updated Name', tipo: 'BANDA' as const };
      await artistaService.update(1, updateData);

      expect(api.put).toHaveBeenCalledWith('/v1/artistas/1', updateData);
    });

    it('should return updated artista', async () => {
      const updatedArtista = { ...mockArtista, nome: 'Updated', tipo: 'BANDA' as const };
      vi.mocked(api.put).mockResolvedValue({ data: updatedArtista });

      const result = await artistaService.update(1, { nome: 'Updated', tipo: 'BANDA' });

      expect(result.nome).toBe('Updated');
      expect(result.tipo).toBe('BANDA');
    });

    it('should propagate error on 404', async () => {
      const error = { response: { status: 404, data: { message: 'Not found' } } };
      vi.mocked(api.put).mockRejectedValue(error);

      await expect(artistaService.update(999, { nome: 'Test', tipo: 'CANTOR' })).rejects.toEqual(error);
    });
  });
});
