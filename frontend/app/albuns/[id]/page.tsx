'use client';

import { useEffect, useState, useCallback } from 'react';
import { useParams, useRouter, notFound } from 'next/navigation';
import Link from 'next/link';
import {
  ArrowLeft,
  Pencil,
  Disc3,
  RefreshCw,
  AlertCircle,
  Calendar,
  User,
  Users,
  Trash2,
  Upload,
} from 'lucide-react';
import { PageContainer } from '@/components/layout/PageContainer';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { albumService } from '@/lib/album-service';
import type { Album, CapaAlbum } from '@/types/album';

function AlbumDetailSkeleton() {
  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <Skeleton className="h-10 w-24" />
        <Skeleton className="h-10 w-24" />
      </div>
      <div className="flex flex-col gap-6 md:flex-row">
        <Skeleton className="aspect-square w-full max-w-xs" />
        <div className="flex-1 space-y-4">
          <Skeleton className="h-10 w-3/4" />
          <Skeleton className="h-6 w-1/4" />
          <Skeleton className="h-6 w-1/2" />
        </div>
      </div>
    </div>
  );
}

function CapaImage({
  albumId,
  capa,
  onDelete,
}: {
  albumId: number;
  capa: CapaAlbum;
  onDelete?: () => void;
}) {
  const [url, setUrl] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    const loadUrl = async () => {
      try {
        const response = await albumService.getCapaUrl(albumId, capa.id);
        setUrl(response.url);
      } catch {}
    };
    loadUrl();
  }, [albumId, capa.id]);

  const handleDelete = async () => {
    if (!confirm('Tem certeza que deseja excluir esta capa?')) return;
    setIsDeleting(true);
    try {
      await albumService.deleteCapa(albumId, capa.id);
      onDelete?.();
    } catch {
      alert('Erro ao excluir capa');
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="group relative aspect-square overflow-hidden rounded-lg bg-muted">
      {url ? (
        /* eslint-disable-next-line @next/next/no-img-element */
        <img src={url} alt={capa.originalName} className="h-full w-full object-cover" />
      ) : (
        <div className="flex h-full w-full items-center justify-center">
          <Disc3 className="h-12 w-12 text-muted-foreground" />
        </div>
      )}
      {onDelete && (
        <div className="absolute inset-0 flex items-center justify-center bg-black/50 opacity-0 transition-opacity group-hover:opacity-100">
          <Button variant="destructive" size="sm" onClick={handleDelete} disabled={isDeleting}>
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      )}
    </div>
  );
}

export default function AlbumDetalhesPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;

  const [album, setAlbum] = useState<Album | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [is404, setIs404] = useState(false);

  const fetchAlbum = useCallback(async () => {
    if (!id) return;

    setIsLoading(true);
    setError(null);

    try {
      const data = await albumService.getById(Number(id));
      setAlbum(data);
    } catch (err: unknown) {
      const axiosError = err as { response?: { status?: number } };
      if (axiosError.response?.status === 404) {
        setIs404(true);
      } else {
        setError('Erro ao carregar álbum. Tente novamente.');
      }
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchAlbum();
  }, [fetchAlbum]);

  if (is404) {
    notFound();
  }

  if (isLoading) {
    return (
      <PageContainer>
        <AlbumDetailSkeleton />
      </PageContainer>
    );
  }

  if (error) {
    return (
      <PageContainer>
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <div className="mb-4 rounded-full bg-destructive/10 p-4">
            <AlertCircle className="h-8 w-8 text-destructive" />
          </div>
          <p className="mb-4 text-muted-foreground">{error}</p>
          <Button onClick={fetchAlbum} variant="outline">
            <RefreshCw className="h-4 w-4" />
            Tentar novamente
          </Button>
        </div>
      </PageContainer>
    );
  }

  if (!album) {
    return null;
  }

  const createdAt = new Date(album.createdAt).toLocaleDateString('pt-BR');

  return (
    <PageContainer>
      <div className="space-y-8">
        {/* Action buttons */}
        <div className="flex items-center justify-between">
          <Button variant="ghost" asChild>
            <Link href="/albuns">
              <ArrowLeft className="h-4 w-4" />
              Voltar
            </Link>
          </Button>
          <Button asChild>
            <Link href={`/albuns/${id}/editar`}>
              <Pencil className="h-4 w-4" />
              Editar
            </Link>
          </Button>
        </div>

        {/* Main content */}
        <div className="flex flex-col gap-8 md:flex-row">
          {/* Cover image */}
          <div className="w-full max-w-xs shrink-0">
            {album.capas && album.capas.length > 0 ? (
              <CapaImage albumId={album.id} capa={album.capas[0]} />
            ) : (
              <div className="aspect-square flex items-center justify-center rounded-lg bg-muted">
                <Disc3 className="h-20 w-20 text-muted-foreground" />
              </div>
            )}
          </div>

          {/* Album info */}
          <div className="flex-1 space-y-4">
            <h1 className="text-3xl font-bold">{album.titulo}</h1>

            <div className="flex flex-wrap items-center gap-3">
              <Badge variant="secondary">
                <Calendar className="mr-1 h-3 w-3" />
                {album.anoLancamento}
              </Badge>
              <span className="text-sm text-muted-foreground">Criado em {createdAt}</span>
            </div>

            {/* Artists */}
            <div className="space-y-2">
              <h2 className="text-lg font-semibold">Artistas</h2>
              <div className="flex flex-wrap gap-2">
                {album.artistas.map((artista) => {
                  const Icon = artista.tipo === 'CANTOR' ? User : Users;
                  return (
                    <Card
                      key={artista.id}
                      className="cursor-pointer transition-all hover:shadow-md"
                      onClick={() => router.push(`/artistas/${artista.id}`)}
                    >
                      <CardContent className="flex items-center gap-2 p-3">
                        <Icon className="h-4 w-4 text-muted-foreground" />
                        <span className="font-medium">{artista.nome}</span>
                        <Badge variant="outline" className="text-xs">
                          {artista.tipo === 'CANTOR' ? 'Cantor' : 'Banda'}
                        </Badge>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            </div>
          </div>
        </div>

        {/* Capas gallery */}
        {album.capas && album.capas.length > 1 && (
          <div className="space-y-4">
            <h2 className="text-xl font-semibold">Todas as capas ({album.capas.length})</h2>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
              {album.capas.map((capa) => (
                <CapaImage key={capa.id} albumId={album.id} capa={capa} onDelete={fetchAlbum} />
              ))}
            </div>
          </div>
        )}

        {/* Upload new capa button */}
        <div className="flex justify-center pt-4">
          <Button variant="outline" asChild>
            <Link href={`/albuns/${id}/editar`}>
              <Upload className="mr-2 h-4 w-4" />
              Adicionar capa
            </Link>
          </Button>
        </div>
      </div>
    </PageContainer>
  );
}
