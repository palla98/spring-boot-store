package com.codewithmosh.store.orders;

import com.codewithmosh.store.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class OrderService {

    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;


    public List<OrderDto> getOrders (){
        var customer = authService.getCurrentUser();
        var orders = orderRepository.findByCustomer(customer);

        return orders.stream().map(orderMapper::toDto).toList();

    }

    public OrderDto findById(Long orderId){
        var order = orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);

        var customer = authService.getCurrentUser();
        if (!order.isPlaceBy(customer)) {
            throw new AccessDeniedException("Access denied");
        }
        return orderMapper.toDto(order);
    }
}
