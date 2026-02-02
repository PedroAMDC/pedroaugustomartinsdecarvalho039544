import Link from 'next/link';
import { ArrowLeft, UserX } from 'lucide-react';
import { PageContainer } from '@/components/layout/PageContainer';
import { Button } from '@/components/ui/button';

export default function ArtistaNotFound() {
  return (
    <PageContainer>
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <div className="mb-6 rounded-full bg-[var(--muted)] p-6">
          <UserX className="h-12 w-12 text-[var(--muted-foreground)]" />
        </div>
        <h1 className="mb-2 text-2xl font-bold">Artista n&atilde;o encontrado</h1>
        <p className="mb-6 text-[var(--muted-foreground)]">
          O artista que voc&ecirc; est&aacute; procurando n&atilde;o existe ou foi removido.
        </p>
        <Button asChild>
          <Link href="/artistas">
            <ArrowLeft className="h-4 w-4" />
            Voltar para lista
          </Link>
        </Button>
      </div>
    </PageContainer>
  );
}
