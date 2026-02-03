import Link from 'next/link';
import { ArrowLeft, Home } from 'lucide-react';
import { PageContainer } from '@/components/layout/PageContainer';
import { Button } from '@/components/ui/button';
import { ErrorVinyl } from '@/components/errors/ErrorVinyl';

export default function NotFound() {
  return (
    <PageContainer>
      <div className="flex min-h-[60vh] flex-col items-center justify-center py-16 text-center">
        <div className="mb-8">
          <ErrorVinyl variant="broken" size={220} />
        </div>

        <h1 className="mb-3 text-4xl font-bold tracking-tight md:text-5xl">
          404
        </h1>

        <h2 className="mb-2 text-xl font-semibold text-[var(--muted-foreground)] md:text-2xl">
          Faixa Não Encontrada
        </h2>

        <p className="mb-8 max-w-md text-[var(--muted-foreground)]">
          Parece que essa música saiu do setlist... A página que você
          está procurando não existe ou foi movida para outro lugar.
        </p>

        <div className="flex flex-col gap-3 sm:flex-row">
          <Button asChild variant="default" size="lg">
            <Link href="/">
              <Home className="h-4 w-4" />
              Voltar ao Início
            </Link>
          </Button>

          <Button asChild variant="outline" size="lg">
            <Link href="/artistas">
              <ArrowLeft className="h-4 w-4" />
              Ver Artistas
            </Link>
          </Button>
        </div>

        <p className="mt-12 text-sm text-[var(--muted-foreground)]">
          Código do erro: PAGE_NOT_FOUND
        </p>
      </div>
    </PageContainer>
  );
}
