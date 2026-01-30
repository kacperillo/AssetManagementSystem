import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { ReactNode } from 'react';
import { useAuth } from './useAuth';
import { AuthProvider } from '../context/AuthContext';

// Mock jwt-decode
vi.mock('jwt-decode', () => ({
  jwtDecode: vi.fn((token: string) => {
    if (token === 'valid.admin.token') {
      return { sub: 'admin@example.com', role: 'ADMIN', exp: Math.floor(Date.now() / 1000) + 3600 };
    }
    if (token === 'valid.employee.token') {
      return { sub: 'employee@example.com', role: 'EMPLOYEE', exp: Math.floor(Date.now() / 1000) + 3600 };
    }
    if (token === 'expired.token') {
      return { sub: 'user@example.com', role: 'EMPLOYEE', exp: Math.floor(Date.now() / 1000) - 3600 };
    }
    throw new Error('Invalid token');
  }),
}));

// Mock API
vi.mock('../api/auth', () => ({
  login: vi.fn().mockResolvedValue({ token: 'valid.admin.token' }),
}));

const wrapper = ({ children }: { children: ReactNode }) => (
  <AuthProvider>{children}</AuthProvider>
);

describe('useAuth', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('Hook Usage', () => {
    it('should throw error when used outside AuthProvider', () => {
      expect(() => {
        renderHook(() => useAuth());
      }).toThrow('useAuth must be used within an AuthProvider');
    });

    it('should return auth context when used within AuthProvider', () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current).toBeDefined();
      expect(result.current).toHaveProperty('user');
      expect(result.current).toHaveProperty('token');
      expect(result.current).toHaveProperty('isAuthenticated');
      expect(result.current).toHaveProperty('isAdmin');
      expect(result.current).toHaveProperty('isLoading');
      expect(result.current).toHaveProperty('login');
      expect(result.current).toHaveProperty('logout');
    });
  });

  describe('Initial State', () => {
    it('should have null user initially when no token in localStorage', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.token).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.isAdmin).toBe(false);
    });

    it('should restore user from valid token in localStorage', async () => {
      localStorage.setItem('token', 'valid.admin.token');

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.user).toEqual({ email: 'admin@example.com', role: 'ADMIN' });
      expect(result.current.token).toBe('valid.admin.token');
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.isAdmin).toBe(true);
    });

    it('should clear expired token from localStorage', async () => {
      localStorage.setItem('token', 'expired.token');

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.token).toBeNull();
      expect(localStorage.getItem('token')).toBeNull();
    });

    it('should clear invalid token from localStorage', async () => {
      localStorage.setItem('token', 'invalid.token');

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(localStorage.getItem('token')).toBeNull();
    });
  });

  describe('Login', () => {
    it('should set user and token after successful login', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      await act(async () => {
        await result.current.login({ email: 'admin@example.com', password: 'password' });
      });

      expect(result.current.user).toEqual({ email: 'admin@example.com', role: 'ADMIN' });
      expect(result.current.token).toBe('valid.admin.token');
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.isAdmin).toBe(true);
      expect(localStorage.getItem('token')).toBe('valid.admin.token');
    });
  });

  describe('Logout', () => {
    it('should clear user and token on logout', async () => {
      localStorage.setItem('token', 'valid.admin.token');

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.isAuthenticated).toBe(true);

      act(() => {
        result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.token).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.isAdmin).toBe(false);
      expect(localStorage.getItem('token')).toBeNull();
    });
  });

  describe('Role-based Properties', () => {
    it('should set isAdmin to true for ADMIN role', async () => {
      localStorage.setItem('token', 'valid.admin.token');

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.isAdmin).toBe(true);
    });

    it('should set isAdmin to false for EMPLOYEE role', async () => {
      localStorage.setItem('token', 'valid.employee.token');

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
      });

      expect(result.current.isAdmin).toBe(false);
      expect(result.current.user?.role).toBe('EMPLOYEE');
    });
  });
});
