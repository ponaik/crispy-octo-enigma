package com.intern.orderservice.controller;

import com.intern.orderservice.dto.request.CreateItemRequest;
import com.intern.orderservice.dto.response.ItemResponse;
import com.intern.orderservice.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@Tag(name = "Items", description = "Item Management API")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieves detailed information for a specific item by its unique ID.")
    public ResponseEntity<ItemResponse> getById(@PathVariable Long id) {
        return itemService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all items", description = "Returns a paginated list of all items in the catalog.")
    public Page<ItemResponse> getAll(Pageable pageable) {
        return itemService.getAll(pageable);
    }

    @PostMapping
    @Operation(summary = "Create a new item", description = "Creates a new item. This operation is restricted to users with the admin role.")
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody CreateItemRequest item) {
        return ResponseEntity.ok(itemService.create(item));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an item", description = "Permanently removes an item from the catalog. This operation is restricted to users with the admin role.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

