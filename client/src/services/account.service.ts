import { apiClient } from '@/lib/apiClient';
import { 
  Account, 
  CompleteProfileRequest, 
  CompleteProfileResponse, 
  LoginRequest, 
  LoginResponse, 
  SignupRequest, 
  SignupResponse
} from '@/lib/types';

// Asumiendo que tus endpoints de auth están en /auth
const AUTH_PREFIX = '/api/v1/auth';
// Asumiendo que tu endpoint de perfil está en /tasker
const USER_PREFIX = '/api/v1/tasker';


/**
 * Envía la solicitud de login.
 * ASUNCIÓN: El backend responde estableciendo una cookie HttpOnly.
 */
export const login = (data: LoginRequest): Promise<LoginResponse> => {
  return apiClient<LoginResponse>(`${AUTH_PREFIX}/login`, {
    method: 'POST',
    body: JSON.stringify(data),
  }, false); // withCredentials = false (no hay cookie *antes* de loguearse)
};

/**
 * Envía la solicitud de registro.
 */
export const register = (data: SignupRequest): Promise<SignupResponse> => {
  return apiClient<SignupResponse>(`${AUTH_PREFIX}/register`, {
    method: 'POST',
    body: JSON.stringify(data),
  }, false);
};

/**
 * Obtiene el perfil del usuario actualmente logueado.
 */
export const getMyProfile = (): Promise<Account> => {
  console.log("getMyProfile called");
  return apiClient<Account>(`${USER_PREFIX}/user/me`, {
    method: 'GET',
  }, true); // withCredentials = true (¡necesita la cookie 'jwtToken'!)
};
export const completeProfile = (data: CompleteProfileRequest): Promise<CompleteProfileResponse> => {
  console.log(`completeProfile: ${JSON.stringify(data)}`);
  return apiClient<CompleteProfileResponse>(`${USER_PREFIX}/register`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }, true); // withCredencitals = true
};