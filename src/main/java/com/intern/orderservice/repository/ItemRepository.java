package com.intern.orderservice.repository;

import com.intern.orderservice.model.Item;
import org.springframework.data.repository.ListCrudRepository;

public interface ItemRepository extends ListCrudRepository<Item, Long> {
}
