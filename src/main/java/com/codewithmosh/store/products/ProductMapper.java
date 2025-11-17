package com.codewithmosh.store.products;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    //il seguente passaggio devo farlo manualmente perchè il mapper non mi mappa il categoryId dato che il campo nell entity si chiama category e non categoryId:
    @Mapping(target = "categoryId", source = "category.id")
    ProductDto toDto(Product product);

    Product toEntity(ProductDto productDto);

    //devo escludere l id perchè altrimenti lui mi mappa l id del productDTo che passo(null) e non va bene perchè quando poi fa la save mi da eccezione se passo da null ad un id
    @Mapping(target = "id", ignore = true)
    void update(ProductDto productDto, @MappingTarget Product product);
}
