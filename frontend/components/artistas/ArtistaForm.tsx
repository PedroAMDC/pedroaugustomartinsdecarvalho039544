'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { AxiosError } from 'axios';
import { User, Users, Loader2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { artistaService } from '@/lib/artista-service';
import type { ArtistaDetail, TipoArtista } from '@/types/artista';
import type { ApiError } from '@/types/api';

const artistaSchema = z.object({
  nome: z.string().min(1, 'Nome é obrigatório').max(255, 'Nome deve ter no máximo 255 caracteres'),
  tipo: z.enum(['CANTOR', 'BANDA'], {
    message: 'Tipo é obrigatório',
  }),
});

type ArtistaFormData = z.infer<typeof artistaSchema>;

interface ArtistaFormProps {
  artista?: ArtistaDetail;
  onSuccess?: () => void;
}

export function ArtistaForm({ artista, onSuccess }: ArtistaFormProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const isEditMode = !!artista;
  const title = isEditMode ? 'Editar Artista' : 'Novo Artista';
  const description = isEditMode
    ? 'Atualize os dados do artista'
    : 'Preencha os dados do novo artista';

  const form = useForm<ArtistaFormData>({
    resolver: zodResolver(artistaSchema),
    defaultValues: {
      nome: artista?.nome ?? '',
      tipo: artista?.tipo ?? undefined,
    },
  });

  const { isDirty } = form.formState;

  const onSubmit = async (data: ArtistaFormData) => {
    setIsLoading(true);
    setError(null);

    try {
      if (isEditMode && artista) {
        await artistaService.update(artista.id, {
          nome: data.nome,
          tipo: data.tipo as TipoArtista,
        });
      } else {
        await artistaService.create({
          nome: data.nome,
          tipo: data.tipo as TipoArtista,
        });
      }

      if (onSuccess) {
        onSuccess();
      } else {
        router.push('/artistas');
      }
    } catch (err) {
      const axiosError = err as AxiosError<ApiError>;
      if (axiosError.response?.data?.message) {
        setError(axiosError.response.data.message);
      } else {
        setError('Erro ao salvar artista. Tente novamente.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    if (isDirty) {
      const confirmed = window.confirm('Você tem alterações não salvas. Deseja realmente sair?');
      if (!confirmed) {
        return;
      }
    }
    router.push('/artistas');
  };

  return (
    <div className="flex justify-center">
      <Card className="w-full max-w-lg">
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
                name="nome"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Nome *</FormLabel>
                    <FormControl>
                      <Input placeholder="Nome do artista" disabled={isLoading} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="tipo"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Tipo *</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                      disabled={isLoading}
                    >
                      <FormControl>
                        <SelectTrigger className="w-full">
                          <SelectValue placeholder="Selecione o tipo" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="CANTOR">
                          <User className="h-4 w-4" />
                          Cantor
                        </SelectItem>
                        <SelectItem value="BANDA">
                          <Users className="h-4 w-4" />
                          Banda
                        </SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </CardContent>

            <CardFooter className="flex justify-end gap-4 pt-6">
              <Button type="button" variant="outline" onClick={handleCancel} disabled={isLoading}>
                Cancelar
              </Button>
              <Button type="submit" disabled={isLoading}>
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
