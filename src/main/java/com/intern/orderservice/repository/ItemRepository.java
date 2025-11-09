package com.intern.orderservice.repository;

import com.intern.orderservice.model.Item;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

import java.math.BigDecimal;

public interface ItemRepository extends ListCrudRepository<Item, Long>, ListPagingAndSortingRepository<Item, Long> {
    boolean existsByNameAndPrice(String name, BigDecimal price);
}
