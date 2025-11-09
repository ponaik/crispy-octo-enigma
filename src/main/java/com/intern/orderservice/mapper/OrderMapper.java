package com.intern.orderservice.mapper;


import com.intern.orderservice.dto.request.CreateOrderRequest;
import com.intern.orderservice.dto.response.OrderUserResponse;
import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.model.Order;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring", uses = { OrderItemMapper.class })
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Order toOrder(CreateOrderRequest request, @Context Map<Long, Item> itemsById);

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "user", source = "user")
    OrderUserResponse toOrderUserResponse(Order order, UserResponse user);

    @Mapping(target = "user", source = "userId")
    OrderUserResponse toOrderUserResponseList(Order order, @Context Map<Long, UserResponse> usersById);

    default UserResponse mapUser(Long id, @Context Map<Long, UserResponse> usersById) {
        return usersById.get(id);
    }
}

