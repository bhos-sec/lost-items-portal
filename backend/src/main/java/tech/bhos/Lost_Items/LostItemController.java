package tech.bhos.Lost_Items;

import jakarta.validation.Valid;
import tech.bhos.Lost_Items.dto.LostItemRequest;
import tech.bhos.Lost_Items.model.LostItem;
import tech.bhos.Lost_Items.security.AppUserPrincipal;
import tech.bhos.Lost_Items.service.LostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lost-items")
public class LostItemController {

    @Autowired
    private LostItemService service;

    @GetMapping
    public List<LostItem> getAll() {
        return service.getAllLostItems();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LostItem> getById(@PathVariable Integer id) {
        return service.getLostItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public LostItem create(
            @Valid @RequestBody LostItemRequest item,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return service.addLostItem(item, principal.userId());
    }

    @PutMapping("/{id}")
    public LostItem update(
            @PathVariable Integer id,
            @Valid @RequestBody LostItemRequest item,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return service.updateLostItem(id, item, principal.userId());
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        service.deleteLostItem(id, principal.userId());
    }
}
