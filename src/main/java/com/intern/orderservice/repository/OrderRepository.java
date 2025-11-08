package com.intern.orderservice.repository;

import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.enums.OrderStatus;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends ListCrudRepository<Order, Long> {

    List<Order> findAllByStatusIn(Collection<OrderStatus> statuses);

}
