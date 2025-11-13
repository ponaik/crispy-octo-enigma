package com.intern.orderservice.repository;

import com.intern.orderservice.model.Order;
import com.intern.orderservice.model.enums.OrderStatus;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends ListCrudRepository<Order, Long> {

    List<Order> findAllByStatusIn(Collection<OrderStatus> statuses);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findAllByIdInAndUserId(Collection<Long> ids, Long userId);

    List<Order> findAllByStatusInAndUserId(Collection<OrderStatus> statuses,  Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
