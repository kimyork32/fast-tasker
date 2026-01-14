import { Providers } from './providers';
import "./globals.css";
import ConditionalNavbar from '@/components/shared/ConditionalNavbar'
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Fast Tasker',
  description: 'Fast Tasker',
  keywords: ['Fast Tasker', 'Fast Tasker', 'Fast Tasker'],
  icons: {
    icon: '/favicon-32x32.png',
  },
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body suppressHydrationWarning>
        <Providers>
          <ConditionalNavbar />
          {/* pt-16 para que el contenido no quede debajo del navbar fijo */}
          <main className="pt-16 h-screen">{children}</main>
        </Providers>
      </body>
    </html>
  );
}
