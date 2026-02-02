'use client';

import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';
import { PageContainer } from '@/components/layout';
import { Button } from '@/components/ui/button';
import { ArtistaForm } from '@/components/artistas';

export default function NovoArtistaPage() {
  return (
    <PageContainer>
      <div className="space-y-6">
        <Button variant="ghost" asChild>
          <Link href="/artistas">
            <ArrowLeft className="h-4 w-4" />
            Voltar
          </Link>
        </Button>

        <ArtistaForm />
      </div>
    </PageContainer>
  );
}
