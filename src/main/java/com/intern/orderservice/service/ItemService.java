package com.intern.orderservice.service;

import com.intern.orderservice.dto.request.CreateItemRequest;
import com.intern.orderservice.dto.response.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ItemService {
    Optional<ItemResponse> getById(Long id);

    Page<ItemResponse> getAll(Pageable pageable);

    ItemResponse create(CreateItemRequest request);

    void delete(Long id);
}
