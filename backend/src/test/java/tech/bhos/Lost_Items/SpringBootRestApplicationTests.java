package tech.bhos.Lost_Items;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.bhos.Lost_Items.dto.LostItemRequest;
import tech.bhos.Lost_Items.exception.LostItemNotFoundException;
import tech.bhos.Lost_Items.model.LostItem;
import tech.bhos.Lost_Items.model.LostItemStatus;
import tech.bhos.Lost_Items.repository.LostItemRepo;
import tech.bhos.Lost_Items.service.LostItemService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class SpringBootRestApplicationTests {

    @Autowired
    private LostItemService lostItemService;

    @Autowired
    private Validator validator;

    @Autowired
    private LostItemRepo lostItemRepo;

    @BeforeEach
    void setUp() {
        lostItemRepo.deleteAll();
    }

    @Test
    @DisplayName("Service create persists a lost item")
    void createLostItem() throws Exception {
        LostItemRequest payload = validPayload();
        LostItem created = lostItemService.addLostItem(payload, 1L);

        assertThat(created.getItemId()).isNotNull();
        assertThat(created.getItemName()).isEqualTo(payload.itemName());
        assertThat(created.getCreatedByUserId()).isEqualTo(1L);
        assertThat(lostItemRepo.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Service update throws when item is missing")
    void updateMissingItemReturnsNotFound() throws Exception {
        assertThatThrownBy(() -> lostItemService.updateLostItem(99999, validPayload(), 1L))
                .isInstanceOf(LostItemNotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    @DisplayName("DTO validation rejects invalid payload")
    void createLostItemValidationFailure() throws Exception {
        LostItemRequest payload = new LostItemRequest(
                "Laptop",
                "Silver 13-inch laptop",
                "Engineering building",
                "bad-phone",
                LostItemStatus.STILL_LOOKING
        );
        Set<ConstraintViolation<LostItemRequest>> violations = validator.validate(payload);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(violation -> "founderNumber".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Service delete removes an existing item")
    void deleteLostItem() throws Exception {
        LostItem created = lostItemService.addLostItem(validPayload(), 1L);

        lostItemService.deleteLostItem(created.getItemId(), 1L);
        assertThat(lostItemRepo.findById(created.getItemId())).isEmpty();
    }

    private LostItemRequest validPayload() {
        return new LostItemRequest(
                "Laptop",
                "Silver 13-inch laptop",
                "Engineering building",
                "+12025550123",
                LostItemStatus.STILL_LOOKING
        );
    }

}
