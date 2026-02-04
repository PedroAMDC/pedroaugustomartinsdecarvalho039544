'use client';

import { Skeleton } from '@/components/ui/skeleton';
import { Card, CardContent } from '@/components/ui/card';

interface ArtistaDetailSkeletonProps {
  albumCount?: number;
}

function AlbumCardSkeleton() {
  return (
    <Card>
      <Skeleton className="aspect-square w-full rounded-t-lg rounded-b-none" />
      <CardContent className="p-3">
        <Skeleton className="h-4 w-3/4 mb-1" />
        <Skeleton className="h-3 w-1/4" />
      </CardContent>
    </Card>
  );
}

export function ArtistaDetailSkeleton({ albumCount = 4 }: ArtistaDetailSkeletonProps) {
  return (
    <div className="space-y-8">
      {/* Action buttons skeleton */}
      <div className="flex items-center justify-between">
        <Skeleton className="h-9 w-24" />
        <Skeleton className="h-9 w-20" />
      </div>

      {/* Header section skeleton */}
      <div className="flex items-start gap-6">
        <Skeleton className="h-20 w-20 rounded-full shrink-0" />
        <div className="space-y-3">
          <Skeleton className="h-8 w-48" />
          <div className="flex items-center gap-3">
            <Skeleton className="h-5 w-16 rounded-full" />
            <Skeleton className="h-4 w-32" />
          </div>
        </div>
      </div>

      {/* Albums section skeleton */}
      <div className="space-y-4">
        <Skeleton className="h-6 w-32" />
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {Array.from({ length: albumCount }).map((_, i) => (
            <AlbumCardSkeleton key={i} />
          ))}
        </div>
      </div>
    </div>
  );
}
