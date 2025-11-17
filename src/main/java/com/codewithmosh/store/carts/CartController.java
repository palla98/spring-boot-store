package com.codewithmosh.store.carts;

import com.codewithmosh.store.products.ProductNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/carts")
@Tag(name = "Carts")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartDto> createCart(){

        var cartDto = cartService.createCart();
        return ResponseEntity.status(HttpStatus.CREATED).body(cartDto);
    }


    @PostMapping("/{cartId}/items")
    @Operation(summary = "adds a product to the cart")
    public ResponseEntity<CartItemDto> addToCart(
            @Parameter(description = "the Id of the cart")
            @PathVariable UUID cartId,
            @RequestBody AddItemToCartRequest request
    )
    {
        var cartItemDto = cartService.toDto(cartId, request.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemDto);
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCart(
            @PathVariable UUID cartId
    ){
        var cartDto = cartService.getCart(cartId);
        return ResponseEntity.ok(cartDto);
    }

    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long productId,
            @PathVariable UUID cartId,
            @Valid @RequestBody UpdateCartItemRequest request
    ){
        var cartItemDto = cartService.updateCartItem(cartId, productId, request.getQuantity());
        return ResponseEntity.status(HttpStatus.OK).body(cartItemDto);

    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> deleteCartItem(
            @PathVariable("cartId") UUID cartId,
            @PathVariable("productId") Long productId
    )
    {
        cartService.deleteCartItem(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> clearCart(
            @PathVariable UUID cartId
    )
    {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }



    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleCartNotFound(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error: ", "cart not found"));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleProductNotFound(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("error: ", "product not found in the cart"));
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleCartItemNotFound(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error: ", "cart item not found in the cart"));
    }


}
