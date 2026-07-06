import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';

export default function useApiData(loader, fallback, dependencies = []) {
  const { isAuthenticated } = useAuth();
  const [data, setData] = useState(fallback);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  const refresh = useCallback(() => {
    setReloadKey((value) => value + 1);
  }, []);

  useEffect(() => {
    let active = true;

    async function load() {
      if (!isAuthenticated) {
        setData(fallback);
        return;
      }

      setLoading(true);
      setError('');
      try {
        const result = await loader();
        if (active) setData(result ?? fallback);
      } catch (err) {
        if (active) {
          setError(err.message);
          setData(fallback);
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    load();
    return () => {
      active = false;
    };
  }, [isAuthenticated, reloadKey, ...dependencies]);

  return { data, loading, error, refresh };
}

