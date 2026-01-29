interface Props {
  params: Promise<{ id: string }>;
}

export default async function EditarArtistaPage({ params }: Props) {
  const { id } = await params;

  return (
    <main className="container mx-auto p-8">
      <h1 className="text-3xl font-bold mb-8">Editar Artista</h1>
      <p className="text-muted-foreground">ID: {id}</p>
    </main>
  );
}
