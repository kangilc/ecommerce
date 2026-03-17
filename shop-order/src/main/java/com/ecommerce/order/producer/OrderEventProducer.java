package com.ecommerce.order.producer;

import com.ecommerce.common.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "order-created-topic";

    public void send(OrderCreatedEvent event) {
        log.info("Produce OrderCreatedEvent: orderId={}", event.getOrderId());
        kafkaTemplate.send(TOPIC, event);
    }
}
