package tech.bhos.Lost_Items.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@Entity
public class LostItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;

    private String itemName;
    private String itemDesc;
    private String itemLocation;
    private String founderNumber;
    @Enumerated(EnumType.STRING)
    private LostItemStatus status;
    private Long createdByUserId;

    @PrePersist
    public void ensureDefaults() {
        if (status == null) {
            status = LostItemStatus.STILL_LOOKING;
        }
    }
}
