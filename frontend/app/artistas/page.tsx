'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Plus, RefreshCw, AlertCircle, Music } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { PageContainer } from '@/components/layout';
import { Pagination } from '@/components/common/Pagination';
import {
  ArtistaCard,
  ArtistaTable,
  ArtistaFilters,
  ArtistaListSkeleton,
} from '@/components/artistas';
import { artistaService } from '@/lib/artista-service';
import type { Artista, TipoArtista, ArtistaQueryParams } from '@/types/artista';

const PAGE_SIZE = 12;

export default function ArtistasPage() {
  const router = useRouter();

  // Data state
  const [artistas, setArtistas] = useState<Artista[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter state
  const [search, setSearch] = useState('');
  const [tipo, setTipo] = useState<TipoArtista | ''>('');
  const [direction, setDirection] = useState<'asc' | 'desc'>('asc');
  const [viewMode, setViewMode] = useState<'card' | 'table'>('card');

  // Pagination state
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchArtistas = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params: ArtistaQueryParams = {
        page: page - 1, // API usa 0-indexed
        size: PAGE_SIZE,
        sort: 'nome',
        direction,
      };

      if (search) params.nome = search;
      if (tipo) params.tipo = tipo;

      const response = await artistaService.getAll(params);
      setArtistas(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch {
      setError('Erro ao carregar artistas. Tente novamente.');
    } finally {
      setIsLoading(false);
    }
  }, [page, search, tipo, direction]);

  useEffect(() => {
    fetchArtistas();
  }, [fetchArtistas]);

  // Reset page when filters change
  useEffect(() => {
    setPage(1);
  }, [search, tipo, direction]);

  const handleArtistaClick = (id: number) => {
    router.push(`/artistas/${id}`);
  };

  const handleRetry = () => {
    fetchArtistas();
  };

  return (
    <PageContainer>
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-3xl font-bold">Artistas</h1>
        <Button asChild>
          <Link href="/artistas/novo">
            <Plus className="mr-2 h-4 w-4" />
            Novo Artista
          </Link>
        </Button>
      </div>

      {/* Filters */}
      <div className="mb-6">
        <ArtistaFilters
          search={search}
          onSearchChange={setSearch}
          tipo={tipo}
          onTipoChange={setTipo}
          direction={direction}
          onDirectionChange={setDirection}
          viewMode={viewMode}
          onViewModeChange={setViewMode}
          isLoading={isLoading}
        />
      </div>

      {/* Content */}
      {isLoading ? (
        <ArtistaListSkeleton viewMode={viewMode} count={PAGE_SIZE} />
      ) : error ? (
        <div className="flex flex-col items-center justify-center rounded-lg border border-destructive/50 bg-destructive/10 py-12">
          <AlertCircle className="mb-4 h-12 w-12 text-destructive" />
          <p className="mb-4 text-center text-destructive">{error}</p>
          <Button variant="outline" onClick={handleRetry}>
            <RefreshCw className="mr-2 h-4 w-4" />
            Tentar novamente
          </Button>
        </div>
      ) : artistas.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-lg border py-12">
          <Music className="mb-4 h-12 w-12 text-muted-foreground" />
          <p className="mb-2 text-lg font-medium">Nenhum artista encontrado</p>
          <p className="mb-4 text-center text-muted-foreground">
            {search || tipo
              ? 'Tente ajustar os filtros de busca'
              : 'Comece cadastrando seu primeiro artista'}
          </p>
          {!search && !tipo && (
            <Button asChild>
              <Link href="/artistas/novo">
                <Plus className="mr-2 h-4 w-4" />
                Novo Artista
              </Link>
            </Button>
          )}
        </div>
      ) : viewMode === 'card' ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {artistas.map((artista) => (
            <ArtistaCard
              key={artista.id}
              artista={artista}
              onClick={() => handleArtistaClick(artista.id)}
            />
          ))}
        </div>
      ) : (
        <ArtistaTable artistas={artistas} onRowClick={handleArtistaClick} />
      )}

      {/* Pagination */}
      {!isLoading && !error && artistas.length > 0 && (
        <div className="mt-6">
          <Pagination
            page={page}
            totalPages={totalPages}
            totalItems={totalElements}
            pageSize={PAGE_SIZE}
            onPageChange={setPage}
          />
        </div>
      )}
    </PageContainer>
  );
}
