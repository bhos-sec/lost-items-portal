export interface LostItem {
  itemId: number;
  itemName: string;
  itemDesc: string;
  itemLocation: string;
  founderNumber: string;
  createdByUserId: number;
}

export interface LostItemForm {
  itemName: string;
  itemDesc: string;
  itemLocation: string;
  founderNumber: string;
}
