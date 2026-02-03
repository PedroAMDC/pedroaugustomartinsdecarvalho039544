'use client';

import { useEffect } from 'react';
import { RefreshCw, Home } from 'lucide-react';
import Link from 'next/link';
import { PageContainer } from '@/components/layout/PageContainer';
import { Button } from '@/components/ui/button';
import { ErrorVinyl } from '@/components/errors/ErrorVinyl';

interface ErrorProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function Error({ error, reset }: ErrorProps) {
  useEffect(() => {
    console.error('Application error:', error);
  }, [error]);

  return (
    <PageContainer>
      <div className="flex min-h-[60vh] flex-col items-center justify-center py-16 text-center">
        <div className="mb-8">
          <ErrorVinyl variant="scratched" size={220} />
        </div>

        <h1 className="mb-3 text-3xl font-bold tracking-tight md:text-4xl">
          Ops! Algo deu errado
        </h1>

        <h2 className="mb-2 text-lg font-medium text-[var(--muted-foreground)] md:text-xl">
          A agulha travou no disco...
        </h2>

        <p className="mb-8 max-w-md text-[var(--muted-foreground)]">
          Ocorreu um erro inesperado ao processar sua solicitação.
          Tente novamente ou volte para a página inicial.
        </p>

        <div className="flex flex-col gap-3 sm:flex-row">
          <Button onClick={reset} size="lg">
            <RefreshCw className="h-4 w-4" />
            Tentar Novamente
          </Button>

          <Button asChild variant="outline" size="lg">
            <Link href="/">
              <Home className="h-4 w-4" />
              Voltar ao Início
            </Link>
          </Button>
        </div>

        {error.digest && (
          <p className="mt-12 text-sm text-[var(--muted-foreground)]">
            Código do erro: {error.digest}
          </p>
        )}

        {process.env.NODE_ENV === 'development' && (
          <details className="mt-6 w-full max-w-2xl text-left">
            <summary className="cursor-pointer text-sm text-[var(--muted-foreground)] hover:text-[var(--foreground)]">
              Detalhes do erro (desenvolvimento)
            </summary>
            <pre className="mt-2 overflow-auto rounded-lg bg-[var(--muted)] p-4 text-xs">
              {error.message}
              {error.stack && (
                <>
                  {'\n\n'}
                  {error.stack}
                </>
              )}
            </pre>
          </details>
        )}
      </div>
    </PageContainer>
  );
}
