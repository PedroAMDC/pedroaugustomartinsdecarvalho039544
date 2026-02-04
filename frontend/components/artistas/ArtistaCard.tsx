'use client';

import { User, Users, Disc3 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { Artista } from '@/types/artista';

interface ArtistaCardProps {
  artista: Artista;
  onClick: () => void;
}

export function ArtistaCard({ artista, onClick }: ArtistaCardProps) {
  const Icon = artista.tipo === 'CANTOR' ? User : Users;
  const tipoLabel = artista.tipo === 'CANTOR' ? 'Cantor' : 'Banda';

  return (
    <Card
      className="cursor-pointer transition-all duration-200 hover:shadow-md hover:scale-[1.02] hover:-translate-y-0.5"
      onClick={onClick}
    >
      <CardHeader className="pb-2">
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
              <Icon className="h-5 w-5 text-primary" />
            </div>
            <CardTitle className="text-lg line-clamp-1">{artista.nome}</CardTitle>
          </div>
          <Badge variant="secondary" className="shrink-0">
            {tipoLabel}
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Disc3 className="h-4 w-4" />
          <span>
            {artista.quantidadeAlbuns} {artista.quantidadeAlbuns === 1 ? 'álbum' : 'álbuns'}
          </span>
        </div>
      </CardContent>
    </Card>
  );
}
