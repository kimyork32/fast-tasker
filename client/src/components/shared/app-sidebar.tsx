"use client"

import * as React from "react"
import {
  // Tus iconos existentes
  LayoutDashboard,
  Compass,
  PlusCircle,
  CheckSquare,
  User as UserIcon,
  // Iconos nuevos para el footer
  Settings,
  HelpCircle,
  Search,
  ChevronsUpDown,
  LogOut,
  Sparkles,
  BadgeCheck,
  Wallet,
  Bell,
  MessageCircle
} from "lucide-react"
import { usePathname } from "next/navigation"
import Link from "next/link"
import { useEffect, useState } from 'react'
import { getMyProfile } from '@/services/account.service'

import {
  Sidebar,
  SidebarContent,
  SidebarFooter, // <--- IMPORTANTE
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar"

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { LogoutButton } from './LogoutButton'
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
// use User icon via the aliased import `User as UserIcon` above

// Menús (estáticos)
const navMain = [
  { title: "Dashboard", url: "/dashboard", icon: LayoutDashboard },
  { title: "Explorar Tareas", url: "/tasks", icon: Compass },
  { title: "Crear Tarea", url: "/tasks/new", icon: PlusCircle },
  { title: "Mis Tareas", url: "/tasks/my", icon: CheckSquare },
  { title: "Chat", url: "/chat", icon: MessageCircle },
  { title: "Mi Perfil", url: "/profile", icon: UserIcon },
]

const navSecondary = [
  { title: "Configuración", url: "/settings", icon: Settings },
]

export function AppSidebar() {
  const pathname = usePathname()
  const { isMobile } = useSidebar() // Hook para saber si es móvil (para ajustar el dropdown)
  const [profile, setProfile] = useState<any>(null)

  useEffect(() => {
    let mounted = true
    getMyProfile()
      .then((data) => {
        if (mounted) setProfile(data)
        console.log(`data: ${JSON.stringify(data)}`)
      })
      .catch(() => {
        // ignore errors for now; keep default placeholder
      })
    return () => { mounted = false }
  }, [])

  console.log(`profile: ${JSON.stringify(profile)}`)
  const displayName = profile?.profile?.firstName
    ? `${profile.profile.firstName} ${profile.profile.lastName ?? ''}`
    : profile?.email ?? 'shadcn'

  const displayEmail = profile?.email ?? 'm@example.com'

  const photo = profile?.profile?.photo || profile?.photo || null

  return (
    <Sidebar variant="inset">
      {/* HEADER */}
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton size="lg" asChild>
              <Link href="/dashboard">
                <div className="flex aspect-square size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                  <LayoutDashboard className="size-4" />
                </div>
                <div className="flex flex-col gap-0.5 leading-none">
                  <span className="font-semibold">Fast-Tasker</span>
                  <span className="">v1.0.0</span>
                </div>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>

      {/* CONTENIDO */}
      <SidebarContent>
        
        {/* GRUPO 1: Menú Principal */}
        <SidebarGroup>
          <SidebarGroupLabel>Aplicación</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {navMain.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild isActive={pathname === item.url}>
                    <Link href={item.url}>
                      <item.icon />
                      <span>{item.title}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        {/* GRUPO 2: Secundario (Settings/Help) */}
        {/* className="mt-auto" es el TRUCO que empuja esto hacia abajo 
            justo antes del footer */}
        <SidebarGroup className="mt-auto">
          <SidebarGroupContent>
            <SidebarMenu>
              {navSecondary.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild size="sm">
                    <Link href={item.url}>
                      <item.icon />
                      <span>{item.title}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      {/* FOOTER: Perfil de Usuario */}
      <SidebarFooter>
        <SidebarMenu>
          <SidebarMenuItem>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <SidebarMenuButton
                  size="lg"
                  className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                >
                  <Avatar className="h-8 w-8 rounded-lg">
                    {photo ? <AvatarImage src={photo} alt={displayName} /> : null}
                    <AvatarFallback className="rounded-lg"><UserIcon className="h-4 w-4" /></AvatarFallback>
                  </Avatar>
                  <div className="grid flex-1 text-left text-sm leading-tight">
                    <span className="truncate font-semibold">{displayName}</span>
                    
                  </div>
                  <ChevronsUpDown className="ml-auto size-4" />
                </SidebarMenuButton>
              </DropdownMenuTrigger>
              
              {/* El menú que se abre al hacer clic */}
                <DropdownMenuContent
                className="w-[--radix-dropdown-menu-trigger-width] min-w-56 rounded-lg"
                side={isMobile ? "bottom" : "right"}
                align="end"
                sideOffset={4}
              >
                
                <DropdownMenuGroup>
                  <DropdownMenuItem>
                    <Link href="/wallet" className="flex items-center w-full">
                      <Wallet className="mr-2 h-4 w-4" />
                      <span>Billetera</span>
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem>
                    <Link href="/notifications" className="flex items-center w-full">
                      <Bell className="mr-2 h-4 w-4" />
                      <span>Notificaciones</span>
                    </Link>
                  </DropdownMenuItem>
                </DropdownMenuGroup>
                <DropdownMenuSeparator />
                <DropdownMenuItem>
                  {/* Usar el componente LogoutButton existente para asegurarnos
                      de borrar cookies y redirigir correctamente */}
                  <LogoutButton />
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  )
}