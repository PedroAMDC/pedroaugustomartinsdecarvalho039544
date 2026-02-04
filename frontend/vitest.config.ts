import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./vitest.setup.ts'],
    include: ['**/*.{test,spec}.{ts,tsx}'],
    exclude: ['**/node_modules/**', '**/dist/**', '**/.next/**'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      reportsDirectory: './coverage',
      include: [
        'hooks/**',
        'lib/**',
        'contexts/**',
        'components/common/**',
        'app/(auth)/**',
      ],
      exclude: [
        '**/*.d.ts',
        '**/*.config.*',
        '**/node_modules/**',
        '**/.next/**',
        '**/__tests__/**',
        '**/.gitkeep',
      ],
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './'),
    },
  },
});
