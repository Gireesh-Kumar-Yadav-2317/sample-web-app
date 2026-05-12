package com.onemoretechie.sample.controller;

import com.onemoretechie.sample.domain.Item;
import com.onemoretechie.sample.service.ItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemResponse> list() {
        return service.findAll().stream().map(ItemResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ItemResponse get(@PathVariable Long id) {
        return ItemResponse.from(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest body) {
        Item created = service.create(body.name(), body.quantity());
        return ResponseEntity
                .created(URI.create("/api/items/" + created.getId()))
                .body(ItemResponse.from(created));
    }

    @PutMapping("/{id}")
    public ItemResponse update(@PathVariable Long id, @Valid @RequestBody ItemRequest body) {
        return ItemResponse.from(service.update(id, body.name(), body.quantity()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse("not_found", ex.getMessage()));
    }

    public record ItemRequest(
            @NotBlank @Size(max = 200) String name,
            @Min(0) int quantity) {
    }

    public record ItemResponse(Long id, String name, int quantity, Instant createdAt, Instant updatedAt) {
        public static ItemResponse from(Item item) {
            return new ItemResponse(
                    item.getId(),
                    item.getName(),
                    item.getQuantity(),
                    item.getCreatedAt(),
                    item.getUpdatedAt());
        }
    }

    public record ErrorResponse(String code, String message) {
    }
}
