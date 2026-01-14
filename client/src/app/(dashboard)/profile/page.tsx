// src/app/(dashboard)/profile/page.tsx
"use client";

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Account } from '@/lib/types';
import { getMyProfile } from '@/services/account.service';

export default function Profile() {
  const [profile, setProfile] = useState<Account | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await getMyProfile();
        setProfile(data);
      } catch (err) {
        setError((err as Error).message);
      } finally {
        setIsLoading(false);
      }
    };
    fetchProfile();
  }, [router]);

  // Helpers seguros
  const userEmail = profile?.email || "Usuario";
  const userInitial = userEmail.charAt(0).toUpperCase();
  const userName = userEmail.split('@')[0];

  if (isLoading) return <div className="flex h-screen items-center justify-center"><div className="w-8 h-8 border-2 border-gray-900 border-t-transparent rounded-full animate-spin"></div></div>;
  if (error) return <div className="p-8 text-red-500">Error: {error}</div>;
  if (!profile) return null;

  return (
    <div className="min-h-screen bg-white text-gray-800 font-sans">
      
      <main className="max-w-5xl mx-auto px-4 py-10">
        
        <div className="grid grid-cols-1 md:grid-cols-12 gap-12">

          {/* COLUMNA IZQUIERDA (Identidad) */}
          <aside className="md:col-span-4 lg:col-span-3 space-y-8">
            <div className="text-center">
              <div className="relative inline-block">
                <div className="w-32 h-32 mx-auto bg-gray-100 rounded-full flex items-center justify-center text-4xl text-gray-400 font-bold border border-gray-200">
                  {userInitial}
                </div>
                <span className={`absolute bottom-1 right-2 w-5 h-5 border-2 border-white rounded-full ${profile.status === 'ACTIVE' ? 'bg-green-500' : 'bg-gray-400'}`}></span>
              </div>
              
              <h2 className="mt-4 text-xl font-bold text-gray-900 capitalize">{userName}</h2>
              <div className="text-sm text-gray-500 mt-1">Miembro desde 2025</div>
              
              <div className="flex justify-center items-center gap-1 mt-3 text-yellow-500 text-sm font-bold">
                 <span>★ 5.0</span>
                 <span className="text-gray-400 font-normal">(1 reseña)</span>
              </div>
            </div>
            {/*             
            <div className="border-t border-gray-100 pt-6">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-4">Insignias</h3>
              <ul className="space-y-3">
                <li className="flex items-center gap-3 text-sm text-gray-600">
                  <BadgeIcon active={true} />
                  <span>Email Verificado</span>
                </li>
                <li className="flex items-center gap-3 text-sm text-gray-400">
                  <BadgeIcon active={false} />
                  <span>Pagos (No verificado)</span>
                </li>
                <li className="flex items-center gap-3 text-sm text-gray-400">
                  <BadgeIcon active={false} />
                  <span>Móvil (No verificado)</span>
                </li>
              </ul>
            </div> */}
          </aside>

          {/* COLUMNA DERECHA (Contenido) */}
          <section className="md:col-span-8 lg:col-span-9 space-y-10">
            
            {/* Header */}
            <div className="flex justify-between items-start">
              <div>
                <h1 className="text-3xl font-bold text-gray-900">Sobre mí</h1>
                <p className="text-gray-500 mt-2 flex items-center gap-2">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                  Arequipa, Perú
                </p>
              </div>
              <button className="px-5 py-2 rounded-full border border-gray-300 text-sm font-bold text-gray-700 hover:bg-gray-50 transition">
                Editar perfil
              </button>
            </div>

            <div className="bg-gray-50 p-6 rounded-xl text-gray-600 leading-relaxed border border-gray-100">
              <p>¡Hola! Soy {userName}. Estoy listo para ayudar con tareas de mudanza y limpieza. Soy puntual y responsable.</p>
            </div>

            <div>
              <h3 className="text-lg font-bold text-gray-900 mb-4">Habilidades</h3>
              <div className="flex flex-wrap gap-2">
                <span className="px-4 py-2 bg-gray-100 rounded-full text-sm text-gray-600">Limpieza</span>
                <span className="px-4 py-2 bg-gray-100 rounded-full text-sm text-gray-600">Mudanzas</span>
                {/* <button className="px-4 py-2 border border-dashed border-gray-300 rounded-full text-sm text-gray-400 hover:border-gray-400 hover:text-gray-600 transition">+ Agregar</button> */}
              </div>
            </div>

            {/* =========================================================
                NUEVA SECCIÓN: RESEÑAS (Reemplaza al Email Privado)
               ========================================================= */}
            <div className="pt-10 border-t border-gray-100">
                <div className="flex items-center gap-4 mb-8">
                   <h3 className="text-xl font-bold text-gray-900">Reseñas</h3>
                   <div className="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-xs font-bold flex items-center gap-1">
                      ★ 5.0 (1 evaluación)
                   </div>
                </div>

                {/* Lista de Comentarios */}
                <div className="space-y-8">
                    
                    {/* Ejemplo de Reseña 1 (Mock Data) */}
                    <div className="flex gap-4 items-start">
                        {/* Avatar del Reviewer */}
                        <div className="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold shrink-0">
                            MP
                        </div>
                        
                        <div className="flex-1">
                            <div className="flex justify-between items-start">
                                <div>
                                    <h4 className="font-bold text-gray-900 text-sm">Maria Perez</h4>
                                    <div className="flex text-yellow-400 text-xs mt-0.5 mb-2">★★★★★</div>
                                </div>
                                <span className="text-xs text-gray-400">Hace 2 días</span>
                            </div>
                            
                            <p className="text-gray-600 text-sm leading-relaxed">
                                "{userName} fue excelente. Llegó a tiempo, trajo sus propias herramientas y terminó el trabajo de limpieza mucho más rápido de lo esperado. Muy recomendado para cualquiera en Arequipa."
                            </p>
                            
                            <div className="mt-2 flex items-center gap-2">
                                <span className="text-xs font-bold text-gray-400 uppercase bg-gray-50 px-2 py-1 rounded">Limpieza de dpt</span>
                            </div>
                        </div>
                    </div>

                    {/* Ejemplo de Reseña 2 (Comentado para cuando tengas más) */}
                    {/* <div className="flex gap-4 items-start opacity-50">...</div> */}

                </div>
                
                {/* Botón ver más */}
                <div className="mt-8 text-center">
                   <button className="text-sm font-bold text-gray-500 hover:text-gray-900 transition">
                      Ver todas las reseñas
                   </button>
                </div>
            </div>

          </section>

        </div>
      </main>
    </div>
  );
}

function BadgeIcon({ active }: { active: boolean }) {
  if (active) {
    return (
      <div className="w-6 h-6 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 shrink-0">
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}><path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" /></svg>
      </div>
    );
  }
  return (
    <div className="w-6 h-6 rounded-full bg-gray-100 flex items-center justify-center text-gray-300 shrink-0">
       <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}><path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
    </div>
  );
}