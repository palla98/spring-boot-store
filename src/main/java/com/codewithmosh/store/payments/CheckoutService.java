package com.codewithmosh.store.payments;

import com.codewithmosh.store.orders.Order;
import com.codewithmosh.store.carts.CartEmptyException;
import com.codewithmosh.store.carts.CartNotFoundException;
import com.codewithmosh.store.carts.CartRepository;
import com.codewithmosh.store.orders.OrderRepository;
import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.carts.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    private final AuthService authService;
    private final CartService cartService;

    private final PaymentGateway  paymentGateway;


    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        var cart = cartRepository.findById(request.getCartId()).orElse(null);
        if(cart == null) {
           throw new CartNotFoundException();
        }
        if (cart.isEmpty()){
            throw new CartEmptyException();
        }

        var order = Order.fromCart(cart, authService.getCurrentUser());
        orderRepository.save(order);

        //create a checkout session:
        try {
            var session = paymentGateway.createCheckoutSession(order);
            cartService.clearCart(cart.getId());
            return new CheckoutResponse(order.getId(), session.getCheckoutUrl());

        } catch (PaymentException e) {
            orderRepository.delete(order);
        }
        return new CheckoutResponse(order.getId(), null);
    }


    public void handleWebhookEvent(WebhookRequest request){
        paymentGateway
                .parseWebhookEvent(request)
                .ifPresent(payment -> {
                    var order = orderRepository.findById(payment.getOrderId()).orElseThrow();
                    order.setStatus(payment.getPaymentStatus());
                    orderRepository.save(order);
                });
    }

}
