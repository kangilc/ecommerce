package com.ecommerce.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "Invalid input value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "Entity not found"),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "Payment failed"),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "Out of stock");

    private final HttpStatus status;
    private final String message;
}
