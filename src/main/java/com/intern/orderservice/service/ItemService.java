package com.intern.orderservice.service;

import com.intern.orderservice.dto.request.CreateItemRequest;
import com.intern.orderservice.dto.response.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ItemService {
    @Transactional(readOnly = true)
    Optional<ItemResponse> getById(Long id);

    @Transactional(readOnly = true)
    Page<ItemResponse> getAll(Pageable pageable);

    @PreAuthorize("hasRole('admin')")
    ItemResponse create(CreateItemRequest request);

    @PreAuthorize("hasRole('admin')")
    void delete(Long id);
}
