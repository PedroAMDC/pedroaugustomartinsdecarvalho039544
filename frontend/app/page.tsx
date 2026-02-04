import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { VinylDisc } from '@/components/common/LoadingSpinner';

export default function HomePage() {
  return (
    <div className="flex h-full flex-col items-center justify-center p-8">
      <VinylDisc size={120} spinning={false} showArm />
      <h1 className="mt-8 text-4xl font-bold">Artistas e Álbuns</h1>
      <p className="mt-2 text-muted-foreground">Sistema de gerenciamento de artistas e álbuns</p>
      <div className="mt-8 flex gap-4">
        <Button asChild size="lg">
          <Link href="/artistas">Artistas</Link>
        </Button>
        <Button asChild variant="outline" size="lg">
          <Link href="/albuns">Álbuns</Link>
        </Button>
      </div>
    </div>
  );
}
