'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Users, Disc3 } from 'lucide-react';
import { cn } from '@/lib/utils';

interface NavigationProps {
  className?: string;
  onLinkClick?: () => void;
}

const navItems = [
  { href: '/artistas', label: 'Artistas', icon: Users },
  { href: '/albuns', label: 'Albuns', icon: Disc3 },
];

export function Navigation({ className, onLinkClick }: NavigationProps) {
  const pathname = usePathname();

  return (
    <nav className={cn('flex items-center gap-1', className)}>
      {navItems.map((item) => {
        const isActive = pathname.startsWith(item.href);
        const Icon = item.icon;

        return (
          <Link
            key={item.href}
            href={item.href}
            onClick={onLinkClick}
            className={cn(
              'flex items-center gap-2 px-3 py-2 text-sm font-medium rounded-md transition-colors',
              isActive
                ? 'bg-primary text-primary-foreground'
                : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
            )}
          >
            <Icon className="h-4 w-4" />
            {item.label}
          </Link>
        );
      })}
    </nav>
  );
}
