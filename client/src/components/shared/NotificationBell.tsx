// src/components/notifications/NotificationBell.tsx
'use client';

import { useState, useEffect, useRef } from 'react';
import { Bell } from 'lucide-react';
import {
  getNotifications,
  connectWebSocket,
  disconnectWebSocket,
} from '@/services/notification.service';
import { NotificationResponse } from '@/lib/types';
import Cookies from 'js-cookie';
import { jwtDecode } from 'jwt-decode';
import { Button } from '@/components/ui/button';

export function NotificationBell() {
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  
  // Usamos useRef para evitar reconexiones dobles en modo estricto de React
  const isConnected = useRef(false);

  useEffect(() => {
    const token = Cookies.get('jwtToken');
    if (!token) return;

    let userId: string;
    try {
      const decodedToken: { sub: string; taskerId: string } = jwtDecode(token);
      userId = decodedToken.taskerId;
    } catch (error) {
      console.error("Error al decodificar token:", error);
      return;
    }

    // 1. Definir qué hacer cuando llega una notificación en tiempo real
    const handleLiveNotification = (newNotification: NotificationResponse) => {
      setNotifications((prev) => {
        // Evitar duplicados (por si la API REST y el WS traen la misma casi al mismo tiempo)
        if (prev.some(n => n.id === newNotification.id)) return prev;
        return [newNotification, ...prev];
      });
      setUnreadCount((prev) => prev + 1);
    };

    // 2. Conectar WebSocket (si no está ya conectado)
    if (!isConnected.current) {
      connectWebSocket(userId, handleLiveNotification);
      isConnected.current = true;
    }

    // 3. Obtener historial inicial (REST API)
    getNotifications()
      .then((history) => {
        setNotifications((prevCurrentState) => {
          // AQUÍ ESTÁ LA CLAVE: Fusionar, no reemplazar.
          // 'prevCurrentState' contiene las notificaciones que llegaron por WS mientras cargaba la API.
          
          // Creamos un Set con los IDs actuales para filtrar rápido
          const currentIds = new Set(prevCurrentState.map(n => n.id));
          
          // Solo agregamos del historial las que NO estén ya en el estado
          const uniqueHistory = history.filter(n => !currentIds.has(n.id));
          
          // Ponemos las actuales (WS) primero, seguidas del historial
          return [...prevCurrentState, ...uniqueHistory];
        });

        // Actualizar contador. Asumimos que history trae todo.
        // Si tu backend tiene un campo 'isRead', úsalo: history.filter(n => !n.isRead).length
        // Si no, sumamos al contador actual.
        setUnreadCount(prev => prev + history.length); 
      })
      .catch(console.error);

    // Cleanup al desmontar
    return () => {
      disconnectWebSocket();
      isConnected.current = false;
    };
  }, []); // Array vacío: se ejecuta solo al montar el componente

  return (
    <div className="relative">
      <Button variant="ghost" size="icon" onClick={() => setIsOpen(!isOpen)}>
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute top-0 right-0 block h-4 w-4 rounded-full bg-red-500 text-white text-[10px] flex items-center justify-center ring-2 ring-white">
            {unreadCount}
          </span>
        )}
      </Button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg border z-10">
          <div className="p-3 font-bold border-b">Notificaciones</div>
          <div className="max-h-96 overflow-y-auto">
            {notifications.length > 0 ? (
              notifications.map(notif => (
                <div key={notif.id} className="p-3 border-b hover:bg-gray-50 cursor-pointer">
                  <p className="text-sm">
                    {notif.type === 'OFFER_ACCEPTED'
                        ? `¡Tu oferta para la tarea ha sido aceptada!`
                        : notif.type === 'NEW_MESSAGE'
                        ? `Has recibido un nuevo mensaje.`
                        : `Nueva notificación (${notif.type})`
                    }
                  </p>
                  <p className="text-xs text-gray-400 mt-1">
                    {/* Nota: Verifica si tu backend envía 'createAt' o 'createdAt' */}
                    {new Date(notif.createAt || Date.now()).toLocaleString('es-ES', {
                      day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit'
                    })}
                  </p>
                </div>
              ))
            ) : (
              <p className="p-4 text-sm text-gray-500">No tienes notificaciones.</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
