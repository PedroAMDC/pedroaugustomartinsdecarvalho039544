import Link from 'next/link';
import { Button } from '@/components/ui/button';

export default function HomePage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-8">
      <h1 className="text-4xl font-bold mb-8">Artistas e Albuns</h1>
      <div className="flex gap-4">
        <Button asChild>
          <Link href="/artistas">Artistas</Link>
        </Button>
        <Button asChild variant="outline">
          <Link href="/albuns">Albuns</Link>
        </Button>
      </div>
    </main>
  );
}
