import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { PageContainer } from '@/components/layout';

export default function ArtistasPage() {
  return (
    <PageContainer>
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-3xl font-bold">Artistas</h1>
        <Button asChild>
          <Link href="/artistas/novo">Novo Artista</Link>
        </Button>
      </div>
      <p className="text-muted-foreground">
        Lista de artistas será carregada da API
      </p>
    </PageContainer>
  );
}
