package com.intern.orderservice.mapper;

import com.intern.orderservice.dto.request.CreateItemRequest;
import com.intern.orderservice.dto.response.ItemResponse;
import com.intern.orderservice.model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Item toItem(CreateItemRequest request);

    ItemResponse toItemResponse(Item item);
}
