import { useState, useEffect } from 'react';
import { getMyProfile } from '@/services/account.service';
import { Account } from '@/lib/types';

export const useAuth = () => {
  const [user, setUser] = useState<Account | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        // Asumimos que si hay un token en cookies, podemos pedir el perfil
        if (document.cookie.includes('jwtToken=')) {
          const profile = await getMyProfile();
          setUser(profile);
        }
      } catch (err: any) {
        console.error("Error fetching profile:", err);
        setError(err.message || 'Failed to fetch user profile.');
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, []);

  return { user, loading, error };
};