// --- DTOs de Ubicación (Asumidos de tus DTOs) ---
// (Tus DTOs LocationRequest y LocationResponse no estaban definidos,
// así que he creado uno genérico basado en el uso)
export type LocationData = {
  latitude: number;
  longitude: number;
  address: string;
  zip: number;
};

// --- DTOs de Cuenta ---

export type AccountStatus = 'ACTIVE' | 'PENDING' | 'SUSPENDED'; // Asumiendo los valores de tu Enum

// Basado en AccountResponse
export type Account = {
  id: string; // UUID se maneja como string en JSON
  email: string;
  status: AccountStatus;
};

// Basado en LoginRequest
export type LoginRequest = {
  email: string;
  rawPassword: string;
};

// Basado en LoginResponse
// (Asumimos que el backend establece una cookie HttpOnly
// y este DTO es solo una confirmación)
export type LoginResponse = {
  token: string;
};

// Basado en RegisterAccountRequest
export type SignupRequest = {
  email: string;
  rawPassword: string;
};

export interface SignupResponse {
  id: string;
  email: string;
  token: string;
}

export type ProfileData = {
  id: string;
  firstName: string;
  lastName: string;
  photo?: string;
  about?: string;
  location: LocationData
}

export interface MinimalProfileData {
  id: string;
  firstName: string;
  lastName: string;
  photo?: string;
  about?: string;
  reputation: number;
  clientReviews: number;
  completedTasks: number;
}

export interface ChatProfileResponse {
  id: string;
  firstName: string;
  lastName: string;
  photo?: string;
}

// --- DTOS de Complete Profile ---
export type CompleteProfileRequest = {
  profile: ProfileData;
};

export type CompleteProfileResponse = {
  id: string;
  accountId: string;
  profile: ProfileData;
  token: string;
};

// --- DTOs de Tarea ---

// Basado en TaskRequest
export type TaskRequest = {
  title: string;
  description: string;
  budget: number;
  location: LocationData;
  taskDate: string;
};

export type TaskResponse = {
  id: string;
  title: string;
  description: string;
  budget: number;
  location: LocationData;
  taskDate: string;
  status: string;
  posterId: string;
}

export type TaskCompleteResponse = {
  task: TaskResponse;
  profile: MinimalProfileData;
  numOffers: number;
  numQuestions: number;
}

// --- DTOs de Chat ---
export interface MessageContent {
  text?: string;
  attachmentUrl?: string;
}

export interface Message {
  id: string;
  senderId: string;
  content: MessageContent;
  sentAt: string; // ISO String (Instant)
  isRead: boolean;
}

export interface ConversationSummary {
  conversationId: string;
  taskId: string;
  otherParticipantId: string;
  lastMessageSnippet: string;
  profile: ChatProfileResponse;
}

// Payload para enviar al socket
export interface SendMessageRequest {
  conversationId: string;
  content: {
    text: string;
    attachmentUrl: string | null;
  };
}

// -- DTOs offer ---

export interface OfferRequest {
  price: number;
  description: string;
}


export interface OfferResponse {
  id: string;
  price: number;
  description: string;
  status: string;
  createAt: string;
}

export interface OfferProfileResponse {
  offer: OfferResponse;
  profile: ChatProfileResponse;
}

// ---DTOs answer ---
export interface AnswerRequest {
  questionId: string;
  description: string;
}

export interface AnswerResponse {
  id: string;
  description: string;
  questionId: string;
  answeredAt: string;
  createdAt: string;
}

export interface AnswerProfileResponse {
  answer: AnswerResponse;
  profile: ChatProfileResponse;
}


// --- DTOs question ---

export interface QuestionRequest {
  description: string;
}

export interface QuestionResponse {
  id: string;
  description: string;
  status: string;
  createAt: string;
}

export interface QuestionProfileResponse {
  question: QuestionResponse;
  profile: MinimalProfileData;
  answers: AnswerProfileResponse[];
}

// --- DTOs assignTasker ---
export interface AssignTaskerRequest {
  taskerId: string;
  taskId: string;
  offerId: string;
}

export interface AssignTaskerResponse {
  taskerId: String;
  taskId: String;
}

// ---DTOs notification ---
// Define la forma del objeto Notification que viene del backend
export interface NotificationResponse {
  id: string; // UUID
  receiverTaskerId: string; // UUID
  targetId: string; // UUID (e.g., offerId)
  type: 'OFFER_ACCEPTED' | 'NEW_MESSAGE' | string; // NotificationType
  message?: string; // El mensaje se puede construir en el frontend
  createAt: string; // Instant
}