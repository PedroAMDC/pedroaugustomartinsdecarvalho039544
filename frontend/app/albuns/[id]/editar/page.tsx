'use client';

import { useEffect, useState, useCallback } from 'react';
import { useParams, notFound } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, AlertCircle, RefreshCw } from 'lucide-react';
import { PageContainer } from '@/components/layout';
import { Button } from '@/components/ui/button';
import { AlbumForm } from '@/components/albuns';
import { ArtistaDetailSkeleton } from '@/components/artistas';
import { albumService } from '@/lib/album-service';
import type { Album } from '@/types/album';

export default function EditarAlbumPage() {
  const params = useParams();
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
        <div className="space-y-6">
          <Button variant="ghost" asChild>
            <Link href="/albuns">
              <ArrowLeft className="h-4 w-4" />
              Voltar
            </Link>
          </Button>
          <ArtistaDetailSkeleton albumCount={0} />
        </div>
      </PageContainer>
    );
  }

  if (error) {
    return (
      <PageContainer>
        <div className="space-y-6">
          <Button variant="ghost" asChild>
            <Link href="/albuns">
              <ArrowLeft className="h-4 w-4" />
              Voltar
            </Link>
          </Button>
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="mb-4 rounded-full bg-[var(--destructive)]/10 p-4">
              <AlertCircle className="h-8 w-8 text-[var(--destructive)]" />
            </div>
            <p className="mb-4 text-[var(--muted-foreground)]">{error}</p>
            <Button onClick={fetchAlbum} variant="outline">
              <RefreshCw className="h-4 w-4" />
              Tentar novamente
            </Button>
          </div>
        </div>
      </PageContainer>
    );
  }

  if (!album) {
    return null;
  }

  return (
    <PageContainer>
      <div className="space-y-6">
        <Button variant="ghost" asChild>
          <Link href={`/albuns/${id}`}>
            <ArrowLeft className="h-4 w-4" />
            Voltar
          </Link>
        </Button>

        <AlbumForm album={album} />
      </div>
    </PageContainer>
  );
}
