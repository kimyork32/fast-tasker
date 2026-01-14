"use client";

import React from "react";
import Link from "next/link";
import { NotificationBell } from "./NotificationBell";
import { Button } from "@/components/ui/button"; // Importar el botón de shadcn/ui

export const AirTaskerLogo = () => {
  return (
    <svg fill="none" height="28" viewBox="0 0 32 32" width="28" aria-hidden>
      <path
        clipRule="evenodd"
        d="M17.6482 10.1305L15.8785 7.02583L7.02979 22.5499H10.5278L17.6482 10.1305ZM19.8798 14.0457L18.11 17.1983L19.394 19.4511H16.8453L15.1056 22.5499H24.7272L19.8798 14.0457Z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};

// Navbar público: marca a la izquierda y acciones (Sign up / Log in) a la derecha
export function PublicNavbar() {
  return (
    <nav className="fixed top-0 left-0 w-full h-16 bg-white shadow z-50 flex items-center justify-between p-4">
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2">
          <AirTaskerLogo />
          <p className="font-bold text-inherit">AirTasker</p>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <Link href="/login" passHref>
          <Button variant="ghost">Log in</Button>
        </Link>
        <Link href="/signup" passHref>
          <Button>Sign Up</Button>
        </Link>
      </div>
    </nav>
  );
}

// Navbar para el dashboard: solo la marca (nombre) a la izquierda
export function DashboardNavbar() {
  return (
    <nav className="fixed top-0 left-0 w-full h-16 bg-white shadow z-50 flex items-center justify-between p-4">
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2">
          <AirTaskerLogo />
          <p className="font-semibold text-inherit">AirTasker</p>
        </div>
      </div>
      <div className="flex items-center gap-4">
        <NotificationBell />
      </div>
    </nav>
  );
}

// Exports:
// - `Navbar` (named) remains the dashboard navbar to avoid changing dashboard imports
// - `PublicNavbar` is available for the root layout
export { DashboardNavbar as Navbar };