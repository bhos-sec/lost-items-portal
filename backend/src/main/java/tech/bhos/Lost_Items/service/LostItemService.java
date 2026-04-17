package tech.bhos.Lost_Items.service;

import tech.bhos.Lost_Items.dto.LostItemRequest;
import tech.bhos.Lost_Items.exception.LostItemNotFoundException;
import tech.bhos.Lost_Items.model.LostItem;
import tech.bhos.Lost_Items.repository.LostItemRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LostItemService {

    private LostItemRepo repo;

    public LostItemRepo getRepo() { return repo; }

    @Autowired
    public void setRepo(LostItemRepo repo) {
        this.repo = repo;
    }


    public List<LostItem> getAllLostItems() {
        return repo.findAll();
    }

    public Optional<LostItem> getLostItemById(Integer id) {
        return repo.findById(id);
    }

    public LostItem addLostItem(LostItemRequest request, Long creatorUserId) {
        LostItem item = mapToEntity(request, creatorUserId);
        return repo.save(item);
    }

    public LostItem updateLostItem(Integer id, LostItemRequest request, Long currentUserId) {
        LostItem existingItem = repo.findById(id)
                .orElseThrow(() -> new LostItemNotFoundException(id));
        assertOwner(existingItem, currentUserId);

        existingItem.setItemName(request.itemName());
        existingItem.setItemDesc(request.itemDesc());
        existingItem.setItemLocation(request.itemLocation());
        existingItem.setFounderNumber(request.founderNumber());
        return repo.save(existingItem);
    }

    public void deleteLostItem(Integer id, Long currentUserId) {
        LostItem existingItem = repo.findById(id)
                .orElseThrow(() -> new LostItemNotFoundException(id));
        assertOwner(existingItem, currentUserId);
        repo.deleteById(id);
    }

    private LostItem mapToEntity(LostItemRequest request, Long creatorUserId) {
        return new LostItem(
                null,
                request.itemName(),
                request.itemDesc(),
                request.itemLocation(),
                request.founderNumber(),
                creatorUserId
        );
    }

    private void assertOwner(LostItem item, Long currentUserId) {
        if (!Objects.equals(item.getCreatedByUserId(), currentUserId)) {
            throw new AccessDeniedException("You are not allowed to modify this item");
        }
    }
}
