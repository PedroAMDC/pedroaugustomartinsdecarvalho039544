'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Plus, RefreshCw, AlertCircle, Disc3 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { PageContainer } from '@/components/layout';
import { Pagination } from '@/components/common/Pagination';
import { AlbumCard, AlbumFilters, AlbumListSkeleton } from '@/components/albuns';
import { albumService } from '@/lib/album-service';
import type { Album, AlbumQueryParams } from '@/types/album';
import type { TipoArtista } from '@/types/artista';

const PAGE_SIZE = 12;

export default function AlbunsPage() {
  const router = useRouter();

  const [albuns, setAlbuns] = useState<Album[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [tipoArtista, setTipoArtista] = useState<TipoArtista | ''>('');
  const [direction, setDirection] = useState<'asc' | 'desc'>('asc');

  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchAlbuns = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params: AlbumQueryParams = {
        page: page - 1,
        size: PAGE_SIZE,
        direction,
      };

      if (tipoArtista) params.tipoArtista = tipoArtista;

      const response = await albumService.getAll(params);
      setAlbuns(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch {
      setError('Erro ao carregar álbuns. Tente novamente.');
    } finally {
      setIsLoading(false);
    }
  }, [page, tipoArtista, direction]);

  useEffect(() => {
    fetchAlbuns();
  }, [fetchAlbuns]);

  useEffect(() => {
    setPage(1);
  }, [tipoArtista, direction]);

  const handleAlbumClick = (id: number) => {
    router.push(`/albuns/${id}`);
  };

  const handleRetry = () => {
    fetchAlbuns();
  };

  return (
    <PageContainer>
      {/* Header */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-3xl font-bold">Álbuns</h1>
        <Button asChild>
          <Link href="/albuns/novo">
            <Plus className="mr-2 h-4 w-4" />
            Novo Álbum
          </Link>
        </Button>
      </div>

      {/* Filters */}
      <div className="mb-6">
        <AlbumFilters
          tipoArtista={tipoArtista}
          onTipoArtistaChange={setTipoArtista}
          direction={direction}
          onDirectionChange={setDirection}
        />
      </div>

      {/* Content */}
      {isLoading ? (
        <AlbumListSkeleton count={PAGE_SIZE} />
      ) : error ? (
        <div className="flex flex-col items-center justify-center rounded-lg border border-destructive/50 bg-destructive/10 py-12">
          <AlertCircle className="mb-4 h-12 w-12 text-destructive" />
          <p className="mb-4 text-center text-destructive">{error}</p>
          <Button variant="outline" onClick={handleRetry}>
            <RefreshCw className="mr-2 h-4 w-4" />
            Tentar novamente
          </Button>
        </div>
      ) : albuns.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-lg border py-12">
          <Disc3 className="mb-4 h-12 w-12 text-muted-foreground" />
          <p className="mb-2 text-lg font-medium">Nenhum álbum encontrado</p>
          <p className="mb-4 text-center text-muted-foreground">
            {tipoArtista
              ? 'Tente ajustar os filtros de busca'
              : 'Comece cadastrando seu primeiro álbum'}
          </p>
          {!tipoArtista && (
            <Button asChild>
              <Link href="/albuns/novo">
                <Plus className="mr-2 h-4 w-4" />
                Novo Álbum
              </Link>
            </Button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
          {albuns.map((album) => (
            <AlbumCard key={album.id} album={album} onClick={() => handleAlbumClick(album.id)} />
          ))}
        </div>
      )}

      {/* Pagination */}
      {!isLoading && !error && albuns.length > 0 && (
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
