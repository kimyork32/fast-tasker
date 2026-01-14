// URL de tu backend. ¡Debería estar en .env.local!
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081';

/**
 * Cliente 'fetch' centralizado para hablar con tu API de Spring Boot
 */
export const apiClient = async <T>(
  endpoint: string,
  options: RequestInit = {},
  withCredentials = false // Por defecto, no se envían credenciales
) : Promise<T> => {
  const url = `${API_BASE_URL}${endpoint}`;

  // Configuración por defecto
  const config: RequestInit = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    // Si 'withCredentials' es true, usa 'include'.
    // Lo usamos para GET /me, GET /tasks, POST /tasks, etc.
    // Lo dejamos en 'omit' para login/register que no las necesitan.
    credentials: withCredentials ? 'include' : 'omit',
  };

  const res = await fetch(url, config);

  if (!res.ok) {
    // Intentar parsear el error del backend
    const errorData = await res.json().catch(() => ({ 
      message: `Error ${res.status}: ${res.statusText}` 
    }));
    throw new Error(errorData.message || 'Ocurrió un error en la API');
  }

  // Si la respuesta es "No Content" (ej. un DELETE o un POST sin retorno)
  if (res.status === 204) {
    return null as T;
  }

  // Parsear la respuesta JSON
  return res.json() as T;
};