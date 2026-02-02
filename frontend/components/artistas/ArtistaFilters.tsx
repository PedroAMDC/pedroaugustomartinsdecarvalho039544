'use client';

import { LayoutGrid, List } from 'lucide-react';
import { SearchInput } from '@/components/common/SearchInput';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import type { TipoArtista } from '@/types/artista';

interface ArtistaFiltersProps {
  search: string;
  onSearchChange: (value: string) => void;
  tipo: TipoArtista | '';
  onTipoChange: (value: TipoArtista | '') => void;
  direction: 'asc' | 'desc';
  onDirectionChange: (value: 'asc' | 'desc') => void;
  viewMode: 'card' | 'table';
  onViewModeChange: (value: 'card' | 'table') => void;
  isLoading: boolean;
}

export function ArtistaFilters({
  search,
  onSearchChange,
  tipo,
  onTipoChange,
  direction,
  onDirectionChange,
  viewMode,
  onViewModeChange,
  isLoading,
}: ArtistaFiltersProps) {
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
      <SearchInput
        value={search}
        onChange={onSearchChange}
        placeholder="Buscar por nome..."
        isLoading={isLoading}
        className="flex-1 sm:max-w-xs"
      />

      <div className="flex flex-wrap items-center gap-2">
        <Select
          value={tipo || 'ALL'}
          onValueChange={(value) => onTipoChange(value === 'ALL' ? '' : (value as TipoArtista))}
        >
          <SelectTrigger className="w-[130px]">
            <SelectValue placeholder="Tipo" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            <SelectItem value="CANTOR">Cantor</SelectItem>
            <SelectItem value="BANDA">Banda</SelectItem>
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

        <div className="flex rounded-md border">
          <Button
            variant={viewMode === 'card' ? 'secondary' : 'ghost'}
            size="icon"
            className="rounded-r-none"
            onClick={() => onViewModeChange('card')}
            aria-label="Visualizar em cards"
          >
            <LayoutGrid className="h-4 w-4" />
          </Button>
          <Button
            variant={viewMode === 'table' ? 'secondary' : 'ghost'}
            size="icon"
            className="rounded-l-none border-l"
            onClick={() => onViewModeChange('table')}
            aria-label="Visualizar em tabela"
          >
            <List className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
