package com.ecommerce.order.service;

import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.producer.OrderEventProducer;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public Long createOrder(OrderRequest request) {
        log.info("Create new Order: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        // 1. Save order to DB (Status: PENDING)
        Order order = Order.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status(OrderStatus.PENDING)
                .build();
        
        Order savedOrder = orderRepository.save(order);

        // 2. Publish OrderCreatedEvent via Kafka
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                request.getProductId(),
                request.getQuantity(),
                request.getAmount() // simplified for this flow
        );
        orderEventProducer.send(event);

        return savedOrder.getId();
    }
}
