package tech.bhos.Lost_Items.dto;

import jakarta.validation.constraints.NotNull;
import tech.bhos.Lost_Items.model.LostItemStatus;

public record LostItemStatusUpdateRequest(
        @NotNull(message = "Status is required")
        LostItemStatus status
) {
}
