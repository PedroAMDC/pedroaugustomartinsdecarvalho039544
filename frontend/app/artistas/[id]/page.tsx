'use client';

import { useEffect, useState, useCallback } from 'react';
import { useParams, useRouter, notFound } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, Pencil, User, Users, Disc3, RefreshCw, AlertCircle } from 'lucide-react';
import { PageContainer } from '@/components/layout/PageContainer';
import { ArtistaDetailSkeleton } from '@/components/artistas';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { artistaService } from '@/lib/artista-service';
import { albumService } from '@/lib/album-service';
import type { ArtistaDetail, AlbumSummary } from '@/types/artista';

function AlbumCard({ album }: { album: AlbumSummary }) {
  const router = useRouter();
  const [capaUrl, setCapaUrl] = useState<string | null>(null);

  // Fetch album details to get cover
  useEffect(() => {
    const loadCapa = async () => {
      try {
        const albumData = await albumService.getById(album.id);
        if (albumData.capas && albumData.capas.length > 0) {
          const capa = albumData.capas[0];
          const response = await albumService.getCapaUrl(album.id, capa.id);
          setCapaUrl(response.url);
        }
      } catch {
        // Silently fail - show placeholder
      }
    };
    loadCapa();
  }, [album.id]);

  return (
    <Card
      className="cursor-pointer overflow-hidden transition-all duration-200 hover:shadow-md hover:scale-[1.02] hover:-translate-y-0.5"
      onClick={() => router.push(`/albuns/${album.id}`)}
    >
      <div className="aspect-square bg-[var(--muted)] flex items-center justify-center">
        {capaUrl ? (
          /* eslint-disable-next-line @next/next/no-img-element */
          <img
            src={capaUrl}
            alt={`Capa de ${album.titulo}`}
            className="h-full w-full object-cover"
          />
        ) : (
          <Disc3 className="h-12 w-12 text-[var(--muted-foreground)]" />
        )}
      </div>
      <CardContent className="p-3">
        <p className="font-medium truncate">{album.titulo}</p>
        <p className="text-sm text-[var(--muted-foreground)]">{album.anoLancamento}</p>
      </CardContent>
    </Card>
  );
}

export default function ArtistaDetalhesPage() {
  const params = useParams();
  const id = params.id as string;

  const [artista, setArtista] = useState<ArtistaDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [is404, setIs404] = useState(false);

  const fetchArtista = useCallback(async () => {
    if (!id) return;

    setIsLoading(true);
    setError(null);

    try {
      const data = await artistaService.getById(Number(id));
      setArtista(data);
    } catch (err: unknown) {
      const axiosError = err as { response?: { status?: number } };
      if (axiosError.response?.status === 404) {
        setIs404(true);
      } else {
        setError('Erro ao carregar artista. Tente novamente.');
      }
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchArtista();
  }, [fetchArtista]);

  if (is404) {
    notFound();
  }

  if (isLoading) {
    return (
      <PageContainer>
        <ArtistaDetailSkeleton />
      </PageContainer>
    );
  }

  if (error) {
    return (
      <PageContainer>
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <div className="mb-4 rounded-full bg-[var(--destructive)]/10 p-4">
            <AlertCircle className="h-8 w-8 text-[var(--destructive)]" />
          </div>
          <p className="mb-4 text-[var(--muted-foreground)]">{error}</p>
          <Button onClick={fetchArtista} variant="outline">
            <RefreshCw className="h-4 w-4" />
            Tentar novamente
          </Button>
        </div>
      </PageContainer>
    );
  }

  if (!artista) {
    return null;
  }

  const Icon = artista.tipo === 'CANTOR' ? User : Users;
  const tipoLabel = artista.tipo === 'CANTOR' ? 'Cantor' : 'Banda';
  const createdAt = new Date(artista.createdAt).toLocaleDateString('pt-BR');

  return (
    <PageContainer>
      <div className="space-y-8">
        {/* Action buttons */}
        <div className="flex items-center justify-between">
          <Button variant="ghost" asChild>
            <Link href="/artistas">
              <ArrowLeft className="h-4 w-4" />
              Voltar
            </Link>
          </Button>
          <Button asChild>
            <Link href={`/artistas/${id}/editar`}>
              <Pencil className="h-4 w-4" />
              Editar
            </Link>
          </Button>
        </div>

        {/* Header section */}
        <div className="flex items-start gap-6">
          <div className="flex h-20 w-20 shrink-0 items-center justify-center rounded-full bg-[var(--muted)]">
            <Icon className="h-10 w-10 text-[var(--muted-foreground)]" />
          </div>
          <div className="space-y-2">
            <h1 className="text-3xl font-bold">{artista.nome}</h1>
            <div className="flex flex-wrap items-center gap-3">
              <Badge variant="secondary">{tipoLabel}</Badge>
              <span className="text-sm text-[var(--muted-foreground)]">
                Criado em {createdAt}
              </span>
            </div>
          </div>
        </div>

        {/* Albums section */}
        <div className="space-y-4">
          <h2 className="text-xl font-semibold">
            Álbuns ({artista.albuns.length})
          </h2>

          {artista.albuns.length === 0 ? (
            <div className="rounded-lg border border-dashed p-8 text-center">
              <Disc3 className="mx-auto mb-3 h-10 w-10 text-[var(--muted-foreground)]" />
              <p className="text-[var(--muted-foreground)]">
                Nenhum álbum cadastrado para este artista.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
              {artista.albuns.map((album) => (
                <AlbumCard key={album.id} album={album} />
              ))}
            </div>
          )}
        </div>
      </div>
    </PageContainer>
  );
}
