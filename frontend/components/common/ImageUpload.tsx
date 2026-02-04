'use client';

import { useState, useRef, useCallback, useEffect } from 'react';
import { Upload, ImageIcon, X, Loader2 } from 'lucide-react';
import { Progress } from '@/components/ui/progress';
import { cn } from '@/lib/utils';

interface ImageUploadProps {
  onUpload: (file: File) => Promise<void>;
  currentImage?: string;
  maxSize?: number;
  accept?: string;
}

const DEFAULT_MAX_SIZE = 5 * 1024 * 1024;
const DEFAULT_ACCEPT = 'image/*';

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function ImageUpload({
  onUpload,
  currentImage,
  maxSize = DEFAULT_MAX_SIZE,
  accept = DEFAULT_ACCEPT,
}: ImageUploadProps) {
  const [isDragging, setIsDragging] = useState(false);
  const [preview, setPreview] = useState<string | null>(currentImage || null);
  const [progress, setProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (currentImage) {
      setPreview(currentImage);
    }
  }, [currentImage]);

  const validateFile = useCallback(
    (file: File): string | null => {
      if (accept === 'image/*') {
        if (!file.type.startsWith('image/')) {
          return 'Arquivo deve ser uma imagem';
        }
      } else {
        const acceptedTypes = accept.split(',').map((t) => t.trim());
        const isValidType = acceptedTypes.some((type) => {
          if (type.endsWith('/*')) {
            const prefix = type.slice(0, -2);
            return file.type.startsWith(prefix);
          }
          return file.type === type;
        });
        if (!isValidType) {
          return `Tipo de arquivo não permitido. Aceitos: ${accept}`;
        }
      }

      if (file.size > maxSize) {
        return `Arquivo muito grande. Tamanho máximo: ${formatFileSize(maxSize)}`;
      }

      return null;
    },
    [accept, maxSize]
  );

  const handleFile = useCallback(
    async (file: File) => {
      setError(null);

      const validationError = validateFile(file);
      if (validationError) {
        setError(validationError);
        return;
      }

      const objectUrl = URL.createObjectURL(file);
      setPreview(objectUrl);

      setIsUploading(true);
      setProgress(0);

      const progressInterval = setInterval(() => {
        setProgress((prev) => {
          if (prev >= 90) {
            clearInterval(progressInterval);
            return prev;
          }
          return prev + 10;
        });
      }, 100);

      try {
        await onUpload(file);
        setProgress(100);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erro ao fazer upload da imagem');
        setPreview(currentImage || null);
        URL.revokeObjectURL(objectUrl);
      } finally {
        clearInterval(progressInterval);
        setIsUploading(false);
        setTimeout(() => setProgress(0), 500);
      }
    },
    [validateFile, onUpload, currentImage]
  );

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      e.stopPropagation();
      setIsDragging(false);

      const files = e.dataTransfer.files;
      if (files.length > 0) {
        handleFile(files[0]);
      }
    },
    [handleFile]
  );

  const handleClick = useCallback(() => {
    if (!isUploading) {
      inputRef.current?.click();
    }
  }, [isUploading]);

  const handleInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = e.target.files;
      if (files && files.length > 0) {
        handleFile(files[0]);
      }
      e.target.value = '';
    },
    [handleFile]
  );

  const handleRemove = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      if (preview && preview !== currentImage) {
        URL.revokeObjectURL(preview);
      }
      setPreview(null);
      setError(null);
    },
    [preview, currentImage]
  );

  return (
    <div className="w-full">
      <div
        role="button"
        tabIndex={0}
        onClick={handleClick}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            handleClick();
          }
        }}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className={cn(
          'relative flex min-h-[200px] cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed p-6 transition-colors',
          isDragging
            ? 'border-primary bg-primary/5'
            : 'border-muted-foreground/25 hover:border-primary/50 hover:bg-muted/50',
          isUploading && 'pointer-events-none opacity-70'
        )}
        aria-label="Área de upload de imagem"
      >
        <input
          ref={inputRef}
          type="file"
          accept={accept}
          onChange={handleInputChange}
          className="hidden"
          aria-hidden="true"
        />

        {preview ? (
          <div className="relative">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={preview}
              alt="Preview da imagem"
              className="max-h-[160px] max-w-full rounded-md object-contain"
            />
            {!isUploading && (
              <button
                type="button"
                onClick={handleRemove}
                className="absolute -right-2 -top-2 rounded-full bg-destructive p-1 text-destructive-foreground shadow-sm hover:bg-destructive/90"
                aria-label="Remover imagem"
              >
                <X className="h-4 w-4" />
              </button>
            )}
          </div>
        ) : (
          <div className="flex flex-col items-center gap-2 text-center">
            {isUploading ? (
              <Loader2 className="h-10 w-10 animate-spin text-muted-foreground" />
            ) : isDragging ? (
              <Upload className="h-10 w-10 text-primary" />
            ) : (
              <ImageIcon className="h-10 w-10 text-muted-foreground" />
            )}
            <div className="space-y-1">
              <p className="text-sm font-medium">
                {isDragging
                  ? 'Solte a imagem aqui'
                  : 'Arraste uma imagem ou clique para selecionar'}
              </p>
              <p className="text-xs text-muted-foreground">
                Tamanho máximo: {formatFileSize(maxSize)}
              </p>
            </div>
          </div>
        )}
      </div>

      {isUploading && progress > 0 && (
        <div className="mt-3">
          <Progress value={progress} className="h-2" />
          <p className="mt-1 text-center text-xs text-muted-foreground">
            {progress < 100 ? 'Enviando...' : 'Concluído!'}
          </p>
        </div>
      )}

      {error && (
        <p className="mt-2 text-sm text-destructive" role="alert">
          {error}
        </p>
      )}
    </div>
  );
}
