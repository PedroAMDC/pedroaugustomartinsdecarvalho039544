import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import { AuthProvider } from '@/contexts/AuthContext';
import { NotificationProvider } from '@/components/common/NotificationToast';
import { Header } from '@/components/layout';
import { Toaster } from '@/components/ui/sonner';
import './globals.css';

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
});

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
});

export const metadata: Metadata = {
  title: 'Artistas e Albuns',
  description: 'Sistema de gerenciamento de artistas e albuns',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR" suppressHydrationWarning>
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <AuthProvider>
          <NotificationProvider>
            <Header />
            <main className="container py-6">{children}</main>
            <Toaster />
          </NotificationProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
