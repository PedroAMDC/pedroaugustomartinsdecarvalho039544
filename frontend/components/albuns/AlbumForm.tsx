'use client';

import { useState, useEffect, useCallback, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { AxiosError } from 'axios';
import { Loader2, X, User, Users } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { Badge } from '@/components/ui/badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { ImageUpload } from '@/components/common/ImageUpload';
import { albumService } from '@/lib/album-service';
import { artistaService } from '@/lib/artista-service';
import type { Album } from '@/types/album';
import type { Artista } from '@/types/artista';
import type { ApiError } from '@/types/api';

const currentYear = new Date().getFullYear();

const albumSchema = z.object({
  titulo: z
    .string()
    .min(1, 'Título é obrigatório')
    .max(255, 'Título deve ter no máximo 255 caracteres'),
  anoLancamento: z
    .number({
      message: 'Ano deve ser um número válido',
    })
    .int('Ano deve ser um número inteiro')
    .min(1900, 'Ano deve ser maior que 1900')
    .max(currentYear + 1, 'Ano inválido'),
  artistaIds: z
    .array(z.number())
    .min(1, 'Selecione pelo menos um artista'),
});

type AlbumFormData = z.infer<typeof albumSchema>;

interface AlbumFormProps {
  album?: Album;
  onSuccess?: () => void;
}

export function AlbumForm({ album, onSuccess }: AlbumFormProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [artistas, setArtistas] = useState<Artista[]>([]);
  const [loadingArtistas, setLoadingArtistas] = useState(true);
  const [capaFile, setCapaFile] = useState<File | null>(null);
  const [currentCapaUrl, setCurrentCapaUrl] = useState<string | null>(null);
  const router = useRouter();

  const isEditMode = !!album;
  const title = isEditMode ? 'Editar Álbum' : 'Novo Álbum';
  const description = isEditMode
    ? 'Atualize os dados do álbum'
    : 'Preencha os dados do novo álbum';

  const form = useForm<AlbumFormData>({
    resolver: zodResolver(albumSchema),
    defaultValues: {
      titulo: album?.titulo ?? '',
      anoLancamento: album?.anoLancamento ?? new Date().getFullYear(),
      artistaIds: album?.artistas?.map((a) => a.id) ?? [],
    },
  });

  const { isDirty } = form.formState;
  const selectedArtistIds = form.watch('artistaIds');
  const capaFileRef = useRef<File | null>(null);

  // Load artists list
  useEffect(() => {
    const loadArtistas = async () => {
      try {
        const response = await artistaService.getAll({ size: 100 });
        setArtistas(response.content);
      } catch {
        setError('Erro ao carregar lista de artistas');
      } finally {
        setLoadingArtistas(false);
      }
    };
    loadArtistas();
  }, []);

  // Load current cover URL in edit mode
  useEffect(() => {
    const loadCapaUrl = async () => {
      if (album && album.capas && album.capas.length > 0) {
        try {
          const capa = album.capas[0];
          const response = await albumService.getCapaUrl(album.id, capa.id);
          setCurrentCapaUrl(response.url);
        } catch {
          // Silently fail - cover might not be accessible
        }
      }
    };
    loadCapaUrl();
  }, [album]);

  const handleImageUpload = useCallback(async (file: File) => {
    setCapaFile(file);
    capaFileRef.current = file;
  }, []);

  const onSubmit = async (data: AlbumFormData) => {
    setIsLoading(true);
    setError(null);

    try {
      let albumId: number;

      if (isEditMode && album) {
        await albumService.update(album.id, {
          titulo: data.titulo,
          anoLancamento: data.anoLancamento,
          artistaIds: data.artistaIds,
        });
        albumId = album.id;
      } else {
        const newAlbum = await albumService.create({
          titulo: data.titulo,
          anoLancamento: data.anoLancamento,
          artistaIds: data.artistaIds,
        });
        albumId = newAlbum.id;
      }

      // Upload cover if selected
      if (capaFileRef.current) {
        await albumService.uploadCapa(albumId, capaFileRef.current);
      }

      if (onSuccess) {
        onSuccess();
      } else {
        router.push('/albuns');
      }
    } catch (err) {
      const axiosError = err as AxiosError<ApiError>;
      if (axiosError.response?.data?.message) {
        setError(axiosError.response.data.message);
      } else {
        setError('Erro ao salvar álbum. Tente novamente.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    if (isDirty || capaFile) {
      const confirmed = window.confirm(
        'Você tem alterações não salvas. Deseja realmente sair?'
      );
      if (!confirmed) {
        return;
      }
    }
    router.push('/albuns');
  };

  const toggleArtist = (artistId: number) => {
    const current = form.getValues('artistaIds');
    if (current.includes(artistId)) {
      form.setValue(
        'artistaIds',
        current.filter((id) => id !== artistId),
        { shouldValidate: true, shouldDirty: true }
      );
    } else {
      form.setValue('artistaIds', [...current, artistId], {
        shouldValidate: true,
        shouldDirty: true,
      });
    }
  };

  const removeArtist = (artistId: number) => {
    const current = form.getValues('artistaIds');
    form.setValue(
      'artistaIds',
      current.filter((id) => id !== artistId),
      { shouldValidate: true, shouldDirty: true }
    );
  };

  const selectedArtistas = artistas.filter((a) =>
    selectedArtistIds.includes(a.id)
  );

  return (
    <div className="flex justify-center">
      <Card className="w-full max-w-2xl">
        <CardHeader>
          <CardTitle className="text-2xl">{title}</CardTitle>
          <CardDescription>{description}</CardDescription>
        </CardHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)}>
            <CardContent className="space-y-6">
              {error && (
                <div className="rounded-md border border-[var(--destructive)]/50 bg-[var(--destructive)]/10 p-3 text-center text-sm text-[var(--destructive)]">
                  {error}
                </div>
              )}

              <FormField
                control={form.control}
                name="titulo"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Título *</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Título do álbum"
                        disabled={isLoading}
                        maxLength={255}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="anoLancamento"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Ano de Lançamento *</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        placeholder={String(currentYear)}
                        disabled={isLoading}
                        min={1900}
                        max={currentYear + 1}
                        {...field}
                        onChange={(e) => {
                          const value = e.target.value;
                          // Limit to 4 digits
                          if (value.length <= 4) {
                            field.onChange(value === '' ? undefined : Number(value));
                          }
                        }}
                        value={field.value ?? ''}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="artistaIds"
                render={() => (
                  <FormItem>
                    <FormLabel>Artistas *</FormLabel>
                    {selectedArtistas.length > 0 && (
                      <div className="flex flex-wrap gap-2 pb-2">
                        {selectedArtistas.map((artista) => (
                          <Badge
                            key={artista.id}
                            variant="secondary"
                            className="flex items-center gap-1"
                          >
                            {artista.tipo === 'CANTOR' ? (
                              <User className="h-3 w-3" />
                            ) : (
                              <Users className="h-3 w-3" />
                            )}
                            {artista.nome}
                            <button
                              type="button"
                              onClick={() => removeArtist(artista.id)}
                              className="ml-1 rounded-full hover:bg-[var(--muted)]"
                              disabled={isLoading}
                            >
                              <X className="h-3 w-3" />
                            </button>
                          </Badge>
                        ))}
                      </div>
                    )}
                    <div className="max-h-48 overflow-y-auto rounded-md border p-3">
                      {loadingArtistas ? (
                        <div className="flex items-center justify-center py-4">
                          <Loader2 className="h-5 w-5 animate-spin text-[var(--muted-foreground)]" />
                          <span className="ml-2 text-sm text-[var(--muted-foreground)]">
                            Carregando artistas...
                          </span>
                        </div>
                      ) : artistas.length === 0 ? (
                        <p className="py-4 text-center text-sm text-[var(--muted-foreground)]">
                          Nenhum artista cadastrado
                        </p>
                      ) : (
                        <div className="space-y-2">
                          {artistas.map((artista) => (
                            <label
                              key={artista.id}
                              className="flex cursor-pointer items-center gap-3 rounded-md p-2 hover:bg-[var(--muted)]"
                            >
                              <Checkbox
                                checked={selectedArtistIds.includes(artista.id)}
                                onCheckedChange={() => toggleArtist(artista.id)}
                                disabled={isLoading}
                              />
                              <div className="flex items-center gap-2">
                                {artista.tipo === 'CANTOR' ? (
                                  <User className="h-4 w-4 text-[var(--muted-foreground)]" />
                                ) : (
                                  <Users className="h-4 w-4 text-[var(--muted-foreground)]" />
                                )}
                                <span>{artista.nome}</span>
                                <span className="text-xs text-[var(--muted-foreground)]">
                                  ({artista.tipo === 'CANTOR' ? 'Cantor' : 'Banda'})
                                </span>
                              </div>
                            </label>
                          ))}
                        </div>
                      )}
                    </div>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="space-y-2">
                <FormLabel>Capa do Álbum</FormLabel>
                <ImageUpload
                  onUpload={handleImageUpload}
                  currentImage={currentCapaUrl || undefined}
                />
              </div>
            </CardContent>

            <CardFooter className="flex justify-end gap-4 pt-6">
              <Button
                type="button"
                variant="outline"
                onClick={handleCancel}
                disabled={isLoading}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={isLoading || loadingArtistas}>
                {isLoading ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Salvando...
                  </>
                ) : (
                  'Salvar'
                )}
              </Button>
            </CardFooter>
          </form>
        </Form>
      </Card>
    </div>
  );
}
