package tech.bhos.Lost_Items.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lost_item_status_changes")
public class LostItemStatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusChangeId;

    @Column(nullable = false)
    private Integer lostItemId;

    @Enumerated(EnumType.STRING)
    private LostItemStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LostItemStatus toStatus;

    @Column(nullable = false)
    private Long changedByUserId;

    @Column(nullable = false)
    private Instant changedAt;

    @PrePersist
    public void ensureTimestamp() {
        if (changedAt == null) {
            changedAt = Instant.now();
        }
    }
}
