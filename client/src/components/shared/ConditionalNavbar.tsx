"use client";

import React from 'react'
import { usePathname } from 'next/navigation'
import { PublicNavbar } from './Navbar'

export default function ConditionalNavbar() {
  const pathname = usePathname()

  // Mostrar el navbar público SOLO en la ruta raíz '/'
  if (pathname === '/') return <PublicNavbar />
  return null
}
