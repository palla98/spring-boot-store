package com.codewithmosh.store.users;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring") // per dire a spring di gestirlo
public interface UserMapper {
    //@Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())") //cosi facendo mappo il valore in runtime con lespressione
    UserDto toDto(User user);
    User toEntity(RegisterUserRequest request);
    void update(UpdateUserRequest request, @MappingTarget User user); // fa in automatico l update
}
