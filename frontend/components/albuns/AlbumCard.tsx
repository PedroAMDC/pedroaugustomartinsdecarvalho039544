'use client';

import { useState, useEffect } from 'react';
import { Disc3 } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { albumService } from '@/lib/album-service';
import type { Album } from '@/types/album';

interface AlbumCardProps {
  album: Album;
  onClick?: () => void;
}

export function AlbumCard({ album, onClick }: AlbumCardProps) {
  const [capaUrl, setCapaUrl] = useState<string | null>(null);

  useEffect(() => {
    const loadCapa = async () => {
      if (album.capas && album.capas.length > 0) {
        try {
          const capa = album.capas[0];
          const response = await albumService.getCapaUrl(album.id, capa.id);
          setCapaUrl(response.url);
        } catch {}
      }
    };
    loadCapa();
  }, [album.id, album.capas]);

  const artistasText = album.artistas.map((a) => a.nome).join(', ');

  return (
    <Card
      className="cursor-pointer overflow-hidden transition-all duration-200 hover:shadow-md hover:scale-[1.02] hover:-translate-y-0.5"
      onClick={onClick}
    >
      {/* Cover image area */}
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

      {/* Content */}
      <CardContent className="p-3 space-y-0.5">
        <p className="font-medium truncate" title={album.titulo}>
          {album.titulo}
        </p>
        <p className="text-sm text-[var(--muted-foreground)]">{album.anoLancamento}</p>
        {artistasText && (
          <p className="text-xs text-[var(--muted-foreground)] truncate" title={artistasText}>
            {artistasText}
          </p>
        )}
      </CardContent>
    </Card>
  );
}
