package com.codewithmosh.store.carts;

import com.codewithmosh.store.products.Product;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) //lo settiamo come UUID
    @Column(name = "id")
    private UUID id;

    @Column(name = "date", insertable = false, updatable = false) //con insertable e updatable diciamo ad hibernate di ignorare il campo e generare l sql
    private LocalDate date;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.MERGE, orphanRemoval = true)
    private Set<CartItem> items = new LinkedHashSet<>();

    public BigDecimal getTotalPrice() {
        return items
                .stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public CartItem getItem(Long productId) {
        return items.stream().filter(item -> item.getProduct().getId().equals(productId)).findFirst().orElse(null);
    }

    public CartItem addItem(Product product){
        var cartItem = getItem(product.getId());
        if (cartItem != null){
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        }
        else  {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cartItem.setCart(this);
            items.add(cartItem);
        }
        return cartItem;
    }

    public void removeItem(Long productId){
        var cartItem = getItem(productId);
        if (cartItem != null) {
            items.remove(cartItem);
            cartItem.setCart(null);
        }
    }

    public void clearItems(){
        items.clear();
    }

    public boolean isEmpty(){
        return items.isEmpty();
    }

}