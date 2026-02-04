'use client';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import type { TipoArtista } from '@/types/artista';

interface AlbumFiltersProps {
  tipoArtista: TipoArtista | '';
  onTipoArtistaChange: (value: TipoArtista | '') => void;
  direction: 'asc' | 'desc';
  onDirectionChange: (value: 'asc' | 'desc') => void;
}

export function AlbumFilters({
  tipoArtista,
  onTipoArtistaChange,
  direction,
  onDirectionChange,
}: AlbumFiltersProps) {
  return (
    <div className="flex flex-wrap items-center gap-2">
      <Select
        value={tipoArtista || 'ALL'}
        onValueChange={(value) =>
          onTipoArtistaChange(value === 'ALL' ? '' : (value as TipoArtista))
        }
      >
        <SelectTrigger className="w-[160px]">
          <SelectValue placeholder="Tipo de artista" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">Todos os tipos</SelectItem>
          <SelectItem value="CANTOR">Cantores</SelectItem>
          <SelectItem value="BANDA">Bandas</SelectItem>
        </SelectContent>
      </Select>

      <Select
        value={direction}
        onValueChange={(value) => onDirectionChange(value as 'asc' | 'desc')}
      >
        <SelectTrigger className="w-[100px]">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="asc">A-Z</SelectItem>
          <SelectItem value="desc">Z-A</SelectItem>
        </SelectContent>
      </Select>
    </div>
  );
}
