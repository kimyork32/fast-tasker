// src/app/(dashboard)/dashboard/page.tsx
// Este componente ahora es simple.
// El middleware ya garantizó que el usuario está logueado.

export default function DashboardPage() {
  return (
    <div>
      <h1>Dashboard Principal</h1>
      <p>Bienvenido de nuevo.</p>
      <p>Aquí verás un resumen de tu actividad.</p>
      
      {/* NOTA: Si quieres mostrar "Hola, {user.name}",
        debes convertir esta página en un 'use client'
        y usar useEffect/fetch para llamar a tu endpoint
        '/api/v1/tasker/user/me', 
        exactamente como hiciste en 'profile/page.tsx'.
      */}
    </div>
  );
}