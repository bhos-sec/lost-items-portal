package tech.bhos.Lost_Items.dto;

import tech.bhos.Lost_Items.model.LostItemStatus;

import java.time.Instant;

public record LostItemStatusChangeResponse(
        Long statusChangeId,
        LostItemStatus fromStatus,
        LostItemStatus toStatus,
        Long changedByUserId,
        Instant changedAt
) {
}
