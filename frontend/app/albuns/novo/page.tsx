'use client';

import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';
import { PageContainer } from '@/components/layout';
import { Button } from '@/components/ui/button';
import { AlbumForm } from '@/components/albuns';

export default function NovoAlbumPage() {
  return (
    <PageContainer>
      <div className="space-y-6">
        <Button variant="ghost" asChild>
          <Link href="/albuns">
            <ArrowLeft className="h-4 w-4" />
            Voltar
          </Link>
        </Button>

        <AlbumForm />
      </div>
    </PageContainer>
  );
}
