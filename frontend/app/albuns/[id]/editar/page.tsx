import { PageContainer } from '@/components/layout';

interface Props {
  params: Promise<{ id: string }>;
}

export default async function EditarAlbumPage({ params }: Props) {
  const { id } = await params;

  return (
    <PageContainer>
      <h1 className="mb-8 text-3xl font-bold">Editar Álbum</h1>
      <p className="text-muted-foreground">ID: {id}</p>
    </PageContainer>
  );
}
