package com.codewithmosh.store.carts;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {

    @NotNull(message = "quantity must be provided")
    @Min(value = 1, message = "quantity must be greaten then 1")
    @Max(value = 100, message = "quantity must be less then 100")
    private Integer quantity;

}
