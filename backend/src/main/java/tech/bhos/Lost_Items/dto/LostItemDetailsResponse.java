package tech.bhos.Lost_Items.dto;

import tech.bhos.Lost_Items.model.LostItem;

import java.util.List;

public record LostItemDetailsResponse(
        LostItem item,
        List<LostItemStatusChangeResponse> statusHistory
) {
}
