import { apiClient } from "./client";
import type {
  AuthResponse,
  LoginPayload,
  RegisterPayload,
  User,
} from "../types/auth";

export const authApi = {
  async register(payload: RegisterPayload) {
    const response = await apiClient.post<AuthResponse>("/auth/register", payload);
    return response.data;
  },
  async login(payload: LoginPayload) {
    const response = await apiClient.post<AuthResponse>("/auth/login", payload);
    return response.data;
  },
  async refresh() {
    const response = await apiClient.post<AuthResponse>("/auth/refresh");
    return response.data;
  },
  async logout() {
    await apiClient.post("/auth/logout");
  },
  async me() {
    const response = await apiClient.get<User>("/auth/me");
    return response.data;
  },
};
