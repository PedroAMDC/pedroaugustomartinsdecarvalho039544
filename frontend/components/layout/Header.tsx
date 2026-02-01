'use client';

import Link from 'next/link';
import { Music } from 'lucide-react';
import { Navigation } from './Navigation';
import { UserMenu } from './UserMenu';
import { MobileMenu } from './MobileMenu';

export function Header() {
  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 items-center">
        <MobileMenu />

        <Link href="/" className="flex items-center gap-2 mr-6">
          <Music className="h-6 w-6" />
          <span className="font-bold hidden sm:inline-block">
            Artistas e Albuns
          </span>
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
