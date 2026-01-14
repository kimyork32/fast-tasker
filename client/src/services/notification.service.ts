// src/services/notification.service.ts
import { apiClient } from '@/lib/apiClient';
import { NotificationResponse } from '@/lib/types';
import { Client, IMessage } from '@stomp/stompjs';

let stompClient: Client | null = null;

const API_PREFIX = '/api/v1';
const WEBSOCKET_URL = 'ws://localhost:8081/ws/notifications/websocket'; 


/**
 * Conecta al WebSocket y se suscribe al topic de notificaciones del usuario.
 * @param userId - El ID del usuario que ha iniciado sesión.
 * @param onNotification - Una función callback que se ejecutará cada vez que llegue una notificación.
 * @param authToken - El token JWT para autenticar la conexión.
 */

export const getNotifications = (): Promise<NotificationResponse[]> => {
    return apiClient<NotificationResponse[]>(`${API_PREFIX}/notifications`, {
      method: 'GET',
    }, true);
}

export const connectWebSocket = (
  userId: string, 
  onNotification: (notification: NotificationResponse) => void
) => {
    console.log("connectWebSocket called");
  if (stompClient && stompClient.active) {
    console.log('WebSocket ya está conectado.');
    return;
  }

  const token = document.cookie.split(';').map(c => c.trim()).find(c => c.startsWith('jwtToken='))?.split('=')[1];
  
  if (!token) {
    console.error("No se encontró el token JWT. No se puede conectar a notificaciones.");
    return;
  }

  stompClient = new Client({
    brokerURL: WEBSOCKET_URL,
    connectHeaders: {
      Authorization: `Bearer ${token}`
    },
    debug: (str) => {
      console.log('STOMP: ' + str);
    },
    reconnectDelay: 5000,
  });

  // Cuando la conexión es exitosa
  stompClient.onConnect = (frame) => {
    console.log('Conectado al WebSocket: ' + frame);
    
    const topic = `/topic/notifications/${userId}`;

    // Usamos el userId (taskerId) para suscribirnos a un tópico específico y único
    stompClient?.subscribe(topic, (message: IMessage) => {

      console.log('DEBUG: Mensaje WebSocket recibido (raw):', message.body);
      const notification: NotificationResponse = JSON.parse(message.body);
      console.log('DEBUG: Notificación procesada (parsed):', notification);
      
      // Llama a la función callback para que la UI pueda reaccionar
      onNotification(notification);
    });
  };

  // Manejo de errores
  stompClient.onStompError = (frame) => {
    console.error('Error en el broker: ' + frame.headers['message']);
    console.error('Detalles: ' + frame.body);
  };

  // Inicia la conexión
  stompClient.activate();
};

/**
 * Desconecta el cliente WebSocket.
 */
export const disconnectWebSocket = () => {
  if (stompClient && stompClient.active) {
    stompClient.deactivate();
    console.log('WebSocket desconectado.');
  }
  stompClient = null;
};
