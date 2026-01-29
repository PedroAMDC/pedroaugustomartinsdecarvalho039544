import Link from 'next/link';
import { Button } from '@/components/ui/button';

export default function ArtistasPage() {
  return (
    <main className="container mx-auto p-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Artistas</h1>
        <Button asChild>
          <Link href="/artistas/novo">Novo Artista</Link>
        </Button>
      </div>
      <p className="text-muted-foreground">Lista de artistas sera carregada da API</p>
    </main>
  );
}
