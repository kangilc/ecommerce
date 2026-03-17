package com.ecommerce.payment.consumer;

import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-created-topic", groupId = "payment-group")
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Consume OrderCreatedEvent in Payment Service: orderId={}, amount={}",
                event.getOrderId(), event.getAmount());
        
        try {
            paymentService.processPayment(event.getOrderId(), event.getAmount());
            log.info("Payment processed successfully for orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process payment for orderId={}", event.getOrderId(), e);
            // TODO: Handle failure (e.g., publish PaymentFailedEvent to rollback order)
        }
    }
}
