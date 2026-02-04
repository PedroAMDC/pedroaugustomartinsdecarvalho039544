'use client';

import Link from 'next/link';
import { Navigation } from './Navigation';
import { UserMenu } from './UserMenu';
import { MobileMenu } from './MobileMenu';
import { VinylDisc } from '@/components/common/LoadingSpinner';

export function Header() {
  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="flex h-14 w-full items-center px-4 md:px-6">
        <MobileMenu />

        <Link href="/" className="mr-6 flex items-center gap-2">
          <VinylDisc size={28} spinning={false} />
          <span className="hidden font-bold sm:inline-block">Artistas e Álbuns</span>
        </Link>

        <Navigation className="hidden md:flex" />

        <div className="ml-auto flex items-center gap-2">
          <div className="hidden md:block">
            <UserMenu />
          </div>
        </div>
      </div>
    </header>
  );
}
