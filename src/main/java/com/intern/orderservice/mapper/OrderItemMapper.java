package com.intern.orderservice.mapper;

import com.intern.orderservice.dto.request.CreateOrderItemRequest;
import com.intern.orderservice.dto.response.OrderItemResponse;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.model.OrderItem;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring", uses = { ItemMapper.class })
public interface OrderItemMapper {

    @Mapping(target = "item", source = "item")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "item", source = "itemId")
//    @Mapping(target = "item", ignore = true) // will be set in service after resolving Item
    OrderItem toOrderItem(CreateOrderItemRequest dto, @Context Map<Long, Item> itemsById);

    default Item mapItem(Long id, @Context Map<Long, Item> itemsById) {
        return itemsById.get(id);
    }
}

