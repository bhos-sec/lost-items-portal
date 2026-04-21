export type LostItemStatus = "STILL_LOOKING" | "FOUND" | "CLAIMED";

export interface LostItem {
  itemId: number;
  itemName: string;
  itemDesc: string;
  itemLocation: string;
  founderNumber: string;
  status: LostItemStatus;
  createdByUserId: number;
}

export interface LostItemForm {
  itemName: string;
  itemDesc: string;
  itemLocation: string;
  founderNumber: string;
  status: LostItemStatus;
}

export interface LostItemStatusHistoryEntry {
  statusChangeId: number;
  fromStatus: LostItemStatus | null;
  toStatus: LostItemStatus;
  changedByUserId: number;
  changedAt: string;
}

export interface LostItemDetails {
  item: LostItem;
  statusHistory: LostItemStatusHistoryEntry[];
}
