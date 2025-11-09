package com.intern.orderservice.service.impl;

import com.intern.orderservice.dto.request.CreateItemRequest;
import com.intern.orderservice.dto.response.ItemResponse;
import com.intern.orderservice.exception.ItemAlreadyExistsException;
import com.intern.orderservice.mapper.ItemMapper;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.repository.ItemRepository;
import com.intern.orderservice.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemServiceImpl(ItemRepository itemRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ItemResponse> getById(Long id) {
        return itemRepository.findById(id)
                .map(itemMapper::toItemResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ItemResponse> getAll(Pageable pageable) {
        return itemRepository.findAll(pageable)
                .map(itemMapper::toItemResponse);
    }

    @PreAuthorize("hasRole('admin')")
    @Override
    public ItemResponse create(CreateItemRequest request) {
        if (itemRepository.existsByNameAndPrice(request.name(), request.price())) {
            throw new ItemAlreadyExistsException(request.name(), request.price());
        }
        Item item = itemMapper.toItem(request);
        Item saved = itemRepository.save(item);
        return itemMapper.toItemResponse(saved);
    }

    @PreAuthorize("hasRole('admin')")
    @Override
    public void delete(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new EntityNotFoundException("Item with id " + id + " not found");
        }
        itemRepository.deleteById(id);
    }
}

