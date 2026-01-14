import { NextResponse } from 'next/server'
import { NextRequest } from 'next/server'
import * as jose from 'jose';

const AUTH_COOKIE_NAME = 'jwtToken';

// --- Función de verificación (sin cambios) ---
async function verifyToken(token: string) {
  try {
    const base64Secret = process.env.JWT_SECRET || 'bWlDbGF2ZVNlY3JldGFTdXBlckxhcmdhWUNvbXBsZWphUGFyYUZhc3RUYXNrZXIxMjM0NTY3ODk=';
    const decodedString = atob(base64Secret);
    const secretKey = Uint8Array.from(decodedString, c => c.charCodeAt(0));

    const { payload } = await jose.jwtVerify(token, secretKey);
    return payload as jose.JWTPayload & { profileCompleted: boolean };
  } catch (err) {
    console.error('[middleware] Token inválido o expirado.', (err as Error).message);
    return null;
  }
}

// -------------------------------------------------------------------
// CAMBIO 1: La función DEBE llamarse 'proxy'
// -------------------------------------------------------------------
export async function proxy(req: NextRequest) {
  const { pathname } = req.nextUrl;
  console.log(`[middleware] Ejecutando en ruta: ${pathname}`);
  
  const token = req.cookies.get(AUTH_COOKIE_NAME)?.value;

  // --- Definición de rutas ---
  const authRoutes = ['/login', '/signup'];
  
  // ¡Asegúrate de incluir TODAS tus rutas protegidas!
  const protectedRoutes = [
    '/profile', 
    '/complete-profile',
    '/dashboard',
    '/tasks' // <-- ¡Probablemente quieras proteger esta también!
  ];

  const isAuthRoute = authRoutes.includes(pathname);
  const isProtectedRoute = protectedRoutes.some(route => pathname.startsWith(route));

  // --- LÓGICA PRINCIPAL (sin cambios) ---

  // 1. El usuario TIENE un token
  if (token) {
    const payload = await verifyToken(token);

    // 1a. El token es INVÁLIDO
    if (!payload) {
      const res = isProtectedRoute
        ? NextResponse.redirect(new URL('/login', req.url)) // Corregido: si falla en ruta protegida, ir a login
        : NextResponse.next();
      
      res.cookies.set(AUTH_COOKIE_NAME, '', { maxAge: 0 }); // Borra la cookie mala
      return res;
    }

    // 1b. El token es VÁLIDO
    
    // Si intenta ir a /login o /signup, redirigir
    if (isAuthRoute) {
      console.log('[middleware] Usuario logueado en ruta de auth. Redirigiendo a /profile.');
      return NextResponse.redirect(new URL('/profile', req.url));
    }
    
    // 1c. Lógica de perfil incompleto
    if (payload.profileCompleted === false && !pathname.startsWith('/complete-profile')) {
      console.log('[middleware] Perfil incompleto. Redirigiendo a /complete-profile.');
      return NextResponse.redirect(new URL('/complete-profile', req.url));
    }

    // 1d. Token válido, perfil completo, en ruta protegida. Dejar pasar.
    return NextResponse.next();
  }

  // 2. El usuario NO tiene token
  if (!token) {
    // 2a. Si intenta ir a una ruta protegida -> redirigir a login
    if (isProtectedRoute) {
      console.log('[middleware] No auth token found, redirecting to login.');
      return NextResponse.redirect(new URL('/login', req.url));
    }

    // 2b. Si está en una ruta pública (como /login o /) -> dejar pasar
    return NextResponse.next();
  }
}

// -------------------------------------------------------------------
// CAMBIO 2: El 'matcher' DEBE incluir todas las rutas que
// tu lógica de arriba necesita revisar (incluyendo /dashboard)
// -------------------------------------------------------------------
export const config = {
  /*
   * Coincide con todas las rutas excepto las que comienzan con:
   * - api (rutas API de Next.js)
   * - _next/static (archivos estáticos)
   * - _next/image (imágenes de optimización)
   * - favicon.ico (archivo de favicon)
   *
   * Esto asegura que el middleware se ejecute en TODAS tus páginas
   * (login, signup, profile, dashboard, tasks, etc.)
   */
  matcher: [
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
}