"use client";

import React, { useState, useEffect, useRef } from 'react';
import { 
  ConversationSummary, 
  Message, 
  SendMessageRequest, 
  ChatProfileResponse 
} from '@/lib/types';
import { 
  getInbox, 
  getMessages, 
  connect, 
  disconnect, 
  sendMessage as sendChatMessage 
} from '@/services/chat.service';
import { useAuth } from '@/hooks/useAuth';
import { UserIcon } from 'lucide-react';

export default function ChatPage() {
  const { user: currentUser, loading: authLoading } = useAuth();
  // Estado de la UI
  const [inbox, setInbox] = useState<ConversationSummary[]>([]);
  const [activeChatId, setActiveChatId] = useState<string | null>(null);
  const [activeParticipant, setActiveParticipant] = useState<ChatProfileResponse | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState("");
  // El currentUserId ahora viene del hook useAuth
  const [isConnected, setIsConnected] = useState(false);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  // 1. Cargar Inbox al iniciar
  useEffect(() => {
    fetchInbox();
  }, []);

  // 2. Manejar conexión al seleccionar un chat
  useEffect(() => {
    if (activeChatId) {
      // Encontrar el participante activo para mostrar en la UI
      const currentConversation = inbox.find(c => c.conversationId === activeChatId);
      setActiveParticipant(currentConversation?.profile ?? null);

      // a. Cargar historial antiguo (REST)
      fetchMessages(activeChatId);
      
      // b. Conectar al Socket (Real-time)
      connect(
        activeChatId, 
        handleNewMessage,
        setIsConnected
      );
    }

    // Cleanup: Desconectar si cambiamos de chat o salimos
    return () => {
      disconnect();
      setIsConnected(false);
      setActiveParticipant(null);
    };
  }, [activeChatId]);

  // Scroll automático al último mensaje
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // --- MANEJADORES DE LÓGICA ---

  const fetchInbox = async () => {
    try {
      const inboxData = await getInbox();
      setInbox(inboxData);
    } catch (error) {
      console.error("Error cargando inbox:", error);
    }
  };

  const fetchMessages = async (conversationId: string) => {
    try {
      const history = await getMessages(conversationId);
      setMessages(history);
    } catch (error) {
      console.error("Error cargando historial:", error);
    }
  };

  const handleNewMessage = (newMessage: Message) => {
    setMessages((prev) => {
      // 1. Evitar duplicados si el mensaje ya existe (por ID real)
      if (prev.some(m => m.id === newMessage.id)) return prev;

      // 2. Buscar mensaje optimista correspondiente (ID temporal + mismo contenido + mismo sender)
      const optimisticIndex = prev.findIndex(m => 
        m.id.startsWith('temp-') && 
        m.senderId === newMessage.senderId &&
        m.content.text === newMessage.content.text
      );

      if (optimisticIndex !== -1) {
        // Reemplazar el mensaje optimista con el real (confirmación del servidor)
        const newMessages = [...prev];
        newMessages[optimisticIndex] = newMessage;
        return newMessages;
      }

      // 3. Si no es duplicado ni reemplazo, agregar al final
      return [...prev, newMessage];
    });
  };


  const handleSendMessage = () => {
    if (!inputText.trim() || !activeChatId || !currentUser) return;

    const payload: SendMessageRequest = {
      conversationId: activeChatId,
      content: {
        text: inputText,
        attachmentUrl: null // null si no hay foto
      }
    };
    
    // --- ACTUALIZACIÓN OPTIMISTA ---
    // Mostramos el mensaje inmediatamente para que el usuario vea feedback instantáneo
    const optimisticMessage: Message = {
      id: `temp-${Date.now()}`, // ID temporal hasta que recarguemos el historial
      conversationId: activeChatId,
      senderId: currentUser.id,
      recipientId: activeParticipant?.id || 'unknown',
      content: {
        text: inputText,
        attachmentUrl: null
      },
      sentAt: new Date().toISOString(),
      read: false
    };
    setMessages((prev) => [...prev, optimisticMessage]);

    sendChatMessage(payload);
    setInputText("");
  };

  // --- RENDERIZADO ---

  if (authLoading) {
    return (
      <div className="flex h-full items-center justify-center">
        Cargando tu información de usuario...
      </div>
    );
  }

  return (
    <div className="flex h-full bg-gray-100 -m-6"> {/* Ocupa el 100% del padre y anula el p-6 del layout */}
      
      {/* SIDEBAR (INBOX) */}
      <div className="w-1/3 bg-white border-r border-gray-300 overflow-y-auto">
        <div className="p-4 bg-gray-200 font-bold">Mis Conversaciones</div>
        {inbox.length > 0 ? (
          <ul>
            {inbox.map((chat) => (
              <li 
                key={chat.conversationId}
                onClick={() => setActiveChatId(chat.conversationId)} // Al hacer clic, actualizamos el chat activo
                className={`flex items-center p-4 border-b cursor-pointer hover:bg-gray-50 transition-colors ${activeChatId === chat.conversationId ? 'bg-blue-50' : ''}`}
              >
                {chat.profile.photo ? (
                  <img 
                    src={chat.profile.photo} 
                    alt={`Foto de ${chat.profile.firstName}`}
                    className="w-12 h-12 rounded-full mr-4 object-cover"
                  />
                ) : (
                  <div className="w-12 h-12 rounded-full mr-4 bg-gray-300 flex items-center justify-center">
                    <UserIcon className="w-8 h-8 text-black" />
                  </div>
                )}
                <div className="flex-1">
                  <div className="font-semibold text-gray-800">{chat.profile.firstName} {chat.profile.lastName}</div>
                  <p className="text-sm text-gray-500 truncate">{chat.lastMessageSnippet}</p>
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <div className="p-4 text-center text-gray-500">
            No tienes chats disponibles
          </div>
        )}
      </div>

      {/* CHAT AREA */}
      <div className="w-2/3 flex flex-col">
        {activeChatId ? (
          <>
            {/* HEADER */}
            <div className="p-4 bg-white border-b flex justify-between items-center shadow-sm">
              <div className="flex items-center">
                {activeParticipant?.photo ? (
                  <img 
                    src={activeParticipant.photo} 
                    alt={`Foto de ${activeParticipant.firstName}`}
                    className="w-10 h-10 rounded-full mr-3 object-cover"
                  />
                ) : (
                  <div className="w-10 h-10 rounded-full mr-3 bg-gray-300 flex items-center justify-center">
                    <UserIcon className="w-6 h-6 text-black" />
                  </div>
                )}
                <h2 className="font-bold text-lg text-gray-800">
                  {activeParticipant ? `${activeParticipant.firstName} ${activeParticipant.lastName}` : 'Cargando...'}
                </h2>
              </div>
              <div className={`w-3 h-3 rounded-full ${isConnected ? 'bg-green-500' : 'bg-red-500'}`} title={isConnected ? "Conectado" : "Desconectado"}></div>
            </div>

            {/* MENSAJES */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {messages.map((msg) => {
                const isMe = msg.senderId === currentUser?.id;
                return (
                  <div key={msg.id} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
                    <div className={`max-w-xs md:max-w-md p-3 rounded-lg shadow ${isMe ? 'bg-blue-600 text-white rounded-br-none' : 'bg-white text-gray-800 rounded-bl-none border'}`}>
                      {/* Renderizar Foto si existe */}
                      {msg.content.attachmentUrl && (
                        <img src={msg.content.attachmentUrl} alt="Adjunto" className="mb-2 rounded-lg max-h-48 object-cover"/>
                      )}
                      
                      {/* Renderizar Texto */}
                      {msg.content.text && <p>{msg.content.text}</p>}
                      
                      <span className={`text-xs block text-right mt-1 ${isMe ? 'text-blue-200' : 'text-gray-400'}`}>
                        {new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  </div>
                );
              })}
              <div ref={messagesEndRef} />
            </div>

            {/* INPUT AREA */}
            <div className="p-4 bg-white border-t">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={inputText}
                  onChange={(e) => setInputText(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()}
                  placeholder="Escribe un mensaje..."
                  className="flex-1 border rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-blue-500 text-black"
                />
                <button 
                  onClick={handleSendMessage}
                  disabled={!isConnected}
                  className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 disabled:bg-gray-400 transition"
                >
                  Enviar
                </button>
              </div>
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center text-gray-500">
            Selecciona una conversación para empezar
          </div>
        )}
      </div>
    </div>
  );
}