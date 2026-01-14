import { apiClient } from '@/lib/apiClient';
import { ConversationSummary, Message, SendMessageRequest } from '@/lib/types';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';

const API_PREFIX = '/api/v1/conversations';
const SOCKET_URL = "ws://localhost:8081/ws/chat/websocket";

let stompClient: Client | null = null;
let currentSubscription: StompSubscription | null = null;

/**
 * Obtiene el inbox (lista de conversaciones) del usuario.
 */
export const getInbox = (): Promise<ConversationSummary[]> => {
  return apiClient<ConversationSummary[]>(`${API_PREFIX}/inbox`, {
    method: 'GET',
  }, true);
};

/**
 * Obtiene el historial de mensajes de una conversación específica.
 */
export const getMessages = (conversationId: string): Promise<Message[]> => {
  return apiClient<Message[]>(`${API_PREFIX}/${conversationId}/messages`, {
    method: 'GET',
  }, true);
};

/**
 * Conecta al WebSocket y se suscribe a los mensajes de una conversación.
 * @param conversationId - El ID de la conversación a la que suscribirse.
 * @param onMessageReceived - Callback que se ejecuta cuando se recibe un nuevo mensaje.
 * @param onConnectionChange - Callback para notificar cambios en el estado de la conexión.
 */
export const connect = (
  conversationId: string,
  onMessageReceived: (message: Message) => void,
  onConnectionChange: (isConnected: boolean) => void
): void => {
  const subscribeToTopic = () => {
    // Anula la suscripción al tema anterior si existe
    if (currentSubscription) {
      currentSubscription.unsubscribe();
    }

    const topic = `/topic/conversation.${conversationId}`;
    // subscribe for listen receive messages
    currentSubscription = stompClient?.subscribe(topic, (message: IMessage) => {
      console.log("Nuevo mensaje recibido del WebSocket:", message.body);
      const newMessage: Message = JSON.parse(message.body);
      onMessageReceived(newMessage);
    }) ?? null;
  };

  if (stompClient && stompClient.active) {
    subscribeToTopic();
    return;
  }

  const token = document.cookie.split(';').map(c => c.trim()).find(c => c.startsWith('jwtToken='))?.split('=')[1];
  
  if (!token) {
    console.error("No se encontró el token JWT. No se puede conectar al chat.");
    return;
  }

  stompClient = new Client({
    brokerURL: SOCKET_URL,
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 5000,
    debug: (str) => {
      console.log('STOMP: ' + str);
    },
    onConnect: (frame) => {
      console.log('Conectado al WebSocket de Chat');
      onConnectionChange(true);
      subscribeToTopic();
    },
    onStompError: (frame) => {
      console.error('Error en el broker: ' + frame.headers['message']);
      console.error('Detalles: ' + frame.body);
      onConnectionChange(false);
    },
    onWebSocketClose: () => {
      console.log("WebSocket cerrado");
      onConnectionChange(false);
    }
  });

  stompClient.activate();
};

/**
 * Desconecta del WebSocket.
 */
export const disconnect = (): void => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }
  currentSubscription = null;
};

/**
 * Envía un mensaje a través del WebSocket.
 */
export const sendMessage = (payload: SendMessageRequest): void => {
  console.log("sendMessage called");
  
  if (!stompClient || !stompClient.active) {
    console.error("No se puede enviar el mensaje, el cliente STOMP no está conectado.");
    return;
  }
  
  stompClient.publish({
    destination: "/app/chat/.send",
    body: JSON.stringify(payload)
  });
};
