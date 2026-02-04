'use client';

import { User, Users } from 'lucide-react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import type { Artista } from '@/types/artista';

interface ArtistaTableProps {
  artistas: Artista[];
  onRowClick: (id: number) => void;
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

export function ArtistaTable({ artistas, onRowClick }: ArtistaTableProps) {
  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[50%]">Nome</TableHead>
            <TableHead>Tipo</TableHead>
            <TableHead className="text-center">Álbuns</TableHead>
            <TableHead className="text-right">Criado em</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {artistas.map((artista) => {
            const Icon = artista.tipo === 'CANTOR' ? User : Users;
            const tipoLabel = artista.tipo === 'CANTOR' ? 'Cantor' : 'Banda';

            return (
              <TableRow
                key={artista.id}
                className="cursor-pointer"
                onClick={() => onRowClick(artista.id)}
              >
                <TableCell>
                  <div className="flex items-center gap-3">
                    <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10">
                      <Icon className="h-4 w-4 text-primary" />
                    </div>
                    <span className="font-medium">{artista.nome}</span>
                  </div>
                </TableCell>
                <TableCell>
                  <Badge variant="secondary">{tipoLabel}</Badge>
                </TableCell>
                <TableCell className="text-center">{artista.quantidadeAlbuns}</TableCell>
                <TableCell className="text-right text-muted-foreground">
                  {formatDate(artista.createdAt)}
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </div>
  );
}
