package com.codewithmosh.store.orders;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses =  {Order.class})
public interface OrderMapper {
    OrderDto toDto(Order order);
}
