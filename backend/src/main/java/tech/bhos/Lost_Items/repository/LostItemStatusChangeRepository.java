package tech.bhos.Lost_Items.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.bhos.Lost_Items.model.LostItemStatusChange;

import java.util.List;

@Repository
public interface LostItemStatusChangeRepository extends JpaRepository<LostItemStatusChange, Long> {
    List<LostItemStatusChange> findByLostItemIdOrderByChangedAtDesc(Integer lostItemId);
}
