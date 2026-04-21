import type { LostItem, LostItemDetails, LostItemForm, LostItemStatus } from "../types/lostItem";
import { apiClient } from "./client";

export const lostItemsApi = {
  async getAll() {
    const response = await apiClient.get<LostItem[]>("/lost-items");
    return response.data;
  },
  async getById(itemId: string) {
    const response = await apiClient.get<LostItem>(`/lost-items/${itemId}`);
    return response.data;
  },
  async getDetails(itemId: string) {
    const response = await apiClient.get<LostItemDetails>(
      `/lost-items/${itemId}/details`,
    );
    return response.data;
  },
  async create(payload: LostItemForm) {
    const response = await apiClient.post<LostItem>("/lost-items", payload);
    return response.data;
  },
  async update(itemId: string, payload: LostItemForm) {
    const response = await apiClient.put<LostItem>(
      `/lost-items/${itemId}`,
      payload,
    );
    return response.data;
  },
  async remove(itemId: string) {
    await apiClient.delete(`/lost-items/${itemId}`);
  },
  async updateStatus(itemId: string, status: LostItemStatus) {
    const response = await apiClient.patch<LostItem>(`/lost-items/${itemId}/status`, {
      status,
    });
    return response.data;
  },
};
