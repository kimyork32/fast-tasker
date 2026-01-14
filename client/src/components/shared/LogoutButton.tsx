'use client';

import { useRouter } from 'next/navigation';
import Cookies from 'js-cookie';

export function LogoutButton() {
  const router = useRouter();

  const handleLogout = () => {
    // 1. Borra la cookie de sesión del navegador.
    // Es importante especificar el 'path' para asegurar que se borre la cookie correcta.
    Cookies.remove('jwtToken', { path: '/' });

    console.log('Cookie eliminada. Redirigiendo a /login...');

    // 2. Opcional pero recomendado: Llamar a un endpoint de logout en el backend.
    // Esto es útil si mantienes una lista de tokens activos y quieres invalidarlo
    // inmediatamente en el servidor. Por ahora, lo dejamos comentado.
    //
    // try {
    //   await logoutService(); // Llama a tu API de logout si tienes una.
    // } catch (error) {
    //   console.error("Error al cerrar sesión en el servidor:", error);
    // }

    // 3. Redirige al usuario a la página de login.
    // Usamos `replace` en lugar de `push` para que el usuario no pueda
    // volver a la página anterior (protegida) usando el botón "atrás" del navegador.
    router.replace('/');
  };

  return <button onClick={handleLogout}>Cerrar Sesión</button>;
}
