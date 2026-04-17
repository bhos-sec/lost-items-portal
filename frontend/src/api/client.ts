import axios from "axios";

const baseApiUrl = (import.meta.env.VITE_API_URL ?? "http://localhost:8080/api")
  .replace(/\/+$/, "");

let accessToken: string | null = null;

export const setAccessToken = (token: string | null) => {
  accessToken = token;
};

export const apiClient = axios.create({
  baseURL: baseApiUrl,
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});
