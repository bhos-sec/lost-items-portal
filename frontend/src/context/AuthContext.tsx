import {
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { authApi } from "../api/authApi";
import { setAccessToken } from "../api/client";
import type { LoginPayload, RegisterPayload, User } from "../types/auth";
import type { AuthContextValue } from "./auth-context";
import { AuthContext } from "./auth-context";

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const response = await authApi.refresh();
        setAccessToken(response.accessToken);
        setUser({
          userId: response.userId,
          email: response.email,
          role: response.role,
        });
      } catch {
        setAccessToken(null);
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const login = async (payload: LoginPayload) => {
    const response = await authApi.login(payload);
    setAccessToken(response.accessToken);
    setUser({
      userId: response.userId,
      email: response.email,
      role: response.role,
    });
  };

  const register = async (payload: RegisterPayload) => {
    const response = await authApi.register(payload);
    setAccessToken(response.accessToken);
    setUser({
      userId: response.userId,
      email: response.email,
      role: response.role,
    });
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } finally {
      setAccessToken(null);
      setUser(null);
    }
  };

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: !!user,
      isLoading,
      login,
      register,
      logout,
    }),
    [user, isLoading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
