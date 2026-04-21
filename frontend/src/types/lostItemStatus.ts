import type { LostItemStatus } from "./lostItem";

export const LOST_ITEM_STATUS_OPTIONS: LostItemStatus[] = [
  "STILL_LOOKING",
  "FOUND",
  "CLAIMED",
];

export const LOST_ITEM_STATUS_LABELS: Record<LostItemStatus, string> = {
  STILL_LOOKING: "Still Looking",
  FOUND: "Found",
  CLAIMED: "Claimed",
};
