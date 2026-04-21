package tech.bhos.Lost_Items.service;

import tech.bhos.Lost_Items.dto.LostItemRequest;
import tech.bhos.Lost_Items.dto.LostItemDetailsResponse;
import tech.bhos.Lost_Items.dto.LostItemStatusChangeResponse;
import tech.bhos.Lost_Items.exception.LostItemNotFoundException;
import tech.bhos.Lost_Items.model.LostItem;
import tech.bhos.Lost_Items.model.LostItemStatus;
import tech.bhos.Lost_Items.model.LostItemStatusChange;
import tech.bhos.Lost_Items.repository.LostItemRepo;
import tech.bhos.Lost_Items.repository.LostItemStatusChangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import tech.bhos.Lost_Items.model.UserRole;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LostItemService {

    private LostItemRepo repo;
    private LostItemStatusChangeRepository statusChangeRepository;

    public LostItemRepo getRepo() { return repo; }

    @Autowired
    public void setRepo(LostItemRepo repo) {
        this.repo = repo;
    }

    @Autowired
    public void setStatusChangeRepository(LostItemStatusChangeRepository statusChangeRepository) {
        this.statusChangeRepository = statusChangeRepository;
    }


    public List<LostItem> getAllLostItems() {
        return repo.findAll();
    }

    public Optional<LostItem> getLostItemById(Integer id) {
        return repo.findById(id);
    }

    public LostItem addLostItem(LostItemRequest request, Long creatorUserId) {
        LostItem item = mapToEntity(request, creatorUserId);
        LostItem created = repo.save(item);
        recordStatusChange(created.getItemId(), null, created.getStatus(), creatorUserId);
        return created;
    }

    public LostItem updateLostItem(Integer id, LostItemRequest request, Long currentUserId) {
        LostItem existingItem = repo.findById(id)
                .orElseThrow(() -> new LostItemNotFoundException(id));
        assertOwner(existingItem, currentUserId);
        LostItemStatus previousStatus = existingItem.getStatus();
        LostItemStatus nextStatus = request.status() == null ? previousStatus : request.status();

        existingItem.setItemName(request.itemName());
        existingItem.setItemDesc(request.itemDesc());
        existingItem.setItemLocation(request.itemLocation());
        existingItem.setFounderNumber(request.founderNumber());
        existingItem.setStatus(nextStatus);
        LostItem updated = repo.save(existingItem);
        maybeRecordTransition(updated.getItemId(), previousStatus, nextStatus, currentUserId);
        return updated;
    }

    public LostItem updateLostItemStatus(Integer id, LostItemStatus nextStatus, Long currentUserId, UserRole currentUserRole) {
        LostItem existingItem = repo.findById(id)
                .orElseThrow(() -> new LostItemNotFoundException(id));
        assertOwnerOrAdmin(existingItem, currentUserId, currentUserRole);

        LostItemStatus previousStatus = existingItem.getStatus();
        existingItem.setStatus(nextStatus);
        LostItem saved = repo.save(existingItem);
        maybeRecordTransition(saved.getItemId(), previousStatus, nextStatus, currentUserId);
        return saved;
    }

    public void deleteLostItem(Integer id, Long currentUserId) {
        LostItem existingItem = repo.findById(id)
                .orElseThrow(() -> new LostItemNotFoundException(id));
        assertOwner(existingItem, currentUserId);
        repo.deleteById(id);
    }

    public LostItemDetailsResponse getLostItemDetails(Integer id) {
        LostItem item = repo.findById(id)
                .orElseThrow(() -> new LostItemNotFoundException(id));

        List<LostItemStatusChangeResponse> statusHistory = statusChangeRepository
                .findByLostItemIdOrderByChangedAtDesc(id)
                .stream()
                .map(change -> new LostItemStatusChangeResponse(
                        change.getStatusChangeId(),
                        change.getFromStatus(),
                        change.getToStatus(),
                        change.getChangedByUserId(),
                        change.getChangedAt()
                ))
                .toList();

        return new LostItemDetailsResponse(item, statusHistory);
    }

    private LostItem mapToEntity(LostItemRequest request, Long creatorUserId) {
        return new LostItem(
                null,
                request.itemName(),
                request.itemDesc(),
                request.itemLocation(),
                request.founderNumber(),
                request.status() == null ? LostItemStatus.STILL_LOOKING : request.status(),
                creatorUserId
        );
    }

    private void assertOwner(LostItem item, Long currentUserId) {
        if (!Objects.equals(item.getCreatedByUserId(), currentUserId)) {
            throw new AccessDeniedException("You are not allowed to modify this item");
        }
    }

    private void assertOwnerOrAdmin(LostItem item, Long currentUserId, UserRole currentUserRole) {
        if (currentUserRole == UserRole.ADMIN) {
            return;
        }
        assertOwner(item, currentUserId);
    }

    private void maybeRecordTransition(Integer itemId, LostItemStatus previousStatus, LostItemStatus nextStatus, Long changedByUserId) {
        if (Objects.equals(previousStatus, nextStatus)) {
            return;
        }
        recordStatusChange(itemId, previousStatus, nextStatus, changedByUserId);
    }

    private void recordStatusChange(Integer itemId, LostItemStatus previousStatus, LostItemStatus nextStatus, Long changedByUserId) {
        LostItemStatusChange statusChange = new LostItemStatusChange(
                null,
                itemId,
                previousStatus,
                nextStatus,
                changedByUserId,
                Instant.now()
        );
        statusChangeRepository.save(statusChange);
    }
}
