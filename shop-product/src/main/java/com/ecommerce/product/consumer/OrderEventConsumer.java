package com.ecommerce.product.consumer;

import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ProductService productService;

    @KafkaListener(topics = "order-created-topic", groupId = "product-group")
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Consume OrderCreatedEvent in Product Service: orderId={}, productId={}, qty={}",
                event.getOrderId(), event.getProductId(), event.getQuantity());
        
        try {
            productService.decreaseStock(event.getProductId(), event.getQuantity());
            log.info("Stock decreased successfully for productId={}", event.getProductId());
        } catch (Exception e) {
            log.error("Failed to decrease stock for orderId={}", event.getOrderId(), e);
            // TODO: Handle failure (e.g., publish StockFailedEvent to rollback order)
        }
    }
}
