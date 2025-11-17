package com.codewithmosh.store.orders;

import com.codewithmosh.store.users.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // cosi facendo in un unica query forza il fetch join e prendo anche i product:
    //altrimenti sarebbe stata semplicemente cosi: List<Order> findByCustomer( User customer);
    @EntityGraph(attributePaths = "items.product")
    @Query("SELECT o FROM Order o WHERE o.customer = :customer")
    List<Order> findByCustomer(@Param("customer") User customer);
}