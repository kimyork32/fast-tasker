import React from 'react';
import { AppSidebar } from "@/components/shared/app-sidebar" // <--- Tu nuevo componente
import { Navbar } from '@/components/shared/Navbar';
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar"

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    // SidebarProvider maneja todo el contexto del sidebar (mobile, desktop, cookies)
    <SidebarProvider>
      
      {/* 1. El Sidebar de Shadcn */}
      <AppSidebar />

      {/* 2. El contenido principal */}
      {/* h-screen y pt-16 en el mismo elemento causan overflow. Lo separamos. */}
      <main className="w-full flex flex-col h-screen overflow-hidden">
        
        {/* NAVBAR */}
        {/* Agregamos el SidebarTrigger (Hamburguesa) dentro del Navbar o al lado */}
        <div className="fixed top-0 flex items-center border-b bg-background px-4 py-2 w-full z-40 h-16">
            <SidebarTrigger className="mr-2 md:hidden" />
            <div className="flex-1">
                 <Navbar /> {/* Tu navbar existente */}
            </div>
        </div>

        {/* CONTENIDO SCROLLEABLE */}
        <div className="flex-1 overflow-y-auto p-6"> {/* Solo el padding para el navbar */}
           {children}
        </div>

      </main>
    </SidebarProvider>
  );
}