import { PageContainer } from '@/components/layout/PageContainer';
import { ArtistaDetailSkeleton } from '@/components/artistas';

export default function ArtistaDetailLoading() {
  return (
    <PageContainer>
      <ArtistaDetailSkeleton />
    </PageContainer>
  );
}
