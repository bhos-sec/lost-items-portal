package tech.bhos.Lost_Items.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import tech.bhos.Lost_Items.model.LostItemStatus;

public record LostItemRequest(
        @NotBlank(message = "Item name is required")
        String itemName,
        @NotBlank(message = "Item description is required")
        String itemDesc,
        @NotBlank(message = "Item location is required")
        String itemLocation,
        @NotBlank(message = "Founder phone number is required")
        @Pattern(
                regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$",
                message = "Founder phone number format is invalid"
        )
        String founderNumber,
        LostItemStatus status
) {
}
