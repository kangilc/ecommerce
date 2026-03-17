package com.ecommerce.payment.service;

import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void processPayment(Long orderId, Long amount) {
        log.info("Process payment: orderId={}, amount={}", orderId, amount);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .build();

        paymentRepository.save(payment);
        log.info("Payment processed successfully for orderId={}", orderId);
        
        // TODO: you can publish an event (e.g. PaymentSuccessEvent) back to shop-order to update the Order status to COMPLETED.
    }
}
