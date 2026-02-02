import { PageContainer } from '@/components/layout';

interface Props {
  params: Promise<{ id: string }>;
}

export default async function ArtistaDetalhesPage({ params }: Props) {
  const { id } = await params;

  return (
    <PageContainer>
      <h1 className="mb-8 text-3xl font-bold">Detalhes do Artista</h1>
      <p className="text-muted-foreground">ID: {id}</p>
    </PageContainer>
  );
}
