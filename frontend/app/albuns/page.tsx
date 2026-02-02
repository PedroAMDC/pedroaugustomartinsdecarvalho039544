import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { PageContainer } from '@/components/layout';

export default function AlbunsPage() {
  return (
    <PageContainer>
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-3xl font-bold">Álbuns</h1>
        <Button asChild>
          <Link href="/albuns/novo">Novo Álbum</Link>
        </Button>
      </div>
      <p className="text-muted-foreground">
        Lista de álbuns será carregada da API
      </p>
    </PageContainer>
  );
}
