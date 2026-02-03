'use client';

import { useEffect, useState, useCallback, useRef } from 'react';
import { Clock, AlertTriangle, ExternalLink } from 'lucide-react';
import { ErrorVinyl } from './ErrorVinyl';
import { CountdownTimer } from './CountdownTimer';
import { Button } from '@/components/ui/button';

interface RateLimitErrorProps {
  retryAfter: number;
  onRetry?: () => void;
  onDismiss?: () => void;
}

export function RateLimitError({
  retryAfter,
  onRetry,
  onDismiss,
}: RateLimitErrorProps) {
  const [secondsLeft, setSecondsLeft] = useState(retryAfter);
  const completedRef = useRef(false);
  const onRetryRef = useRef(onRetry);

  useEffect(() => {
    onRetryRef.current = onRetry;
  }, [onRetry]);

  const progress = 1 - secondsLeft / retryAfter;
  const isComplete = secondsLeft <= 0;

  useEffect(() => {
    if (completedRef.current || secondsLeft <= 0) return;

    const timer = setInterval(() => {
      setSecondsLeft((prev) => {
        const next = prev - 1;
        if (next <= 0) {
          clearInterval(timer);
          completedRef.current = true;
          setTimeout(() => onRetryRef.current?.(), 500);
          return 0;
        }
        return next;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [secondsLeft]);

  const handleComplete = useCallback(() => {
    completedRef.current = true;
    setTimeout(() => onRetry?.(), 500);
  }, [onRetry]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/95 backdrop-blur-sm">
      <div className="mx-4 w-full max-w-lg rounded-xl border border-[var(--border)] bg-[var(--card)] p-8 shadow-2xl">
        <div className="flex flex-col items-center text-center">
          <div className="relative mb-6">
            <ErrorVinyl variant="paused" size={180} progress={progress} />

            <div className="absolute -bottom-2 -right-2">
              <div className="rounded-full bg-[var(--destructive)] p-2 shadow-lg">
                <AlertTriangle className="h-5 w-5 text-[var(--destructive-foreground)]" />
              </div>
            </div>
          </div>

          <div className="mb-2 flex items-center gap-2 text-[var(--destructive)]">
            <Clock className="h-5 w-5" />
            <span className="text-sm font-medium uppercase tracking-wider">
              Limite de Requisições
            </span>
          </div>

          <h1 className="mb-3 text-2xl font-bold md:text-3xl">
            {isComplete ? 'Pronto para continuar!' : 'Aguarde um momento'}
          </h1>

          <p className="mb-6 text-[var(--muted-foreground)]">
            Você atingiu o limite de{' '}
            <span className="font-semibold text-[var(--foreground)]">
              10 requisições por minuto
            </span>
            , conforme especificado no{' '}
            <span className="font-semibold text-[var(--foreground)]">
              Edital do Processo Seletivo Simplificado Nº 001/2026/SEPLAG
            </span>
            .
          </p>

          {!isComplete ? (
            <>
              <div className="mb-6">
                <CountdownTimer
                  seconds={retryAfter}
                  onComplete={handleComplete}
                  size="lg"
                />
              </div>

              <p className="text-sm text-[var(--muted-foreground)]">
                A página será atualizada automaticamente
              </p>
            </>
          ) : (
            <div className="flex flex-col gap-3 sm:flex-row">
              <Button onClick={onRetry} size="lg">
                Continuar
              </Button>
              {onDismiss && (
                <Button onClick={onDismiss} variant="outline" size="lg">
                  Fechar
                </Button>
              )}
            </div>
          )}

          <div className="mt-8 rounded-lg bg-[var(--muted)] p-4">
            <p className="text-xs text-[var(--muted-foreground)]">
              <strong>Requisito Técnico Sênior:</strong> Este sistema
              implementa rate limiting de acordo com as especificações
              do edital para proteger a integridade da API e garantir acesso
              justo a todos os usuários.
            </p>
            <a
              href="https://seletivo.seplag.mt.gov.br/ver-edital/397"
              target="_blank"
              rel="noopener noreferrer"
              className="mt-2 inline-flex items-center gap-1 text-xs text-[var(--primary)] hover:underline"
            >
              Ver edital completo
              <ExternalLink className="h-3 w-3" />
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}

export function RateLimitOverlay({
  retryAfter,
  onComplete,
}: {
  retryAfter: number;
  onComplete: () => void;
}) {
  return (
    <RateLimitError retryAfter={retryAfter} onRetry={onComplete} />
  );
}
