package com.ecommerce.product.entity;

import com.ecommerce.common.entity.BaseEntity;
import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer stockQuantity;

    private Long sellerId;

    @Column(length = 2000)
    private String description;

    @Column(length = 500)
    private String tags;

    @Builder
    public Product(String name, Long price, Integer stockQuantity, Long sellerId, String description, String tags) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.sellerId = sellerId;
        this.description = description;
        this.tags = tags;
    }

    public void update(String name, Long price, Integer stockQuantity, Long sellerId, String description, String tags) {
        if (name != null) {
            this.name = name;
        }
        if (price != null) {
            this.price = price;
        }
        if (stockQuantity != null) {
            this.stockQuantity = stockQuantity;
        }
        if (sellerId != null) {
            this.sellerId = sellerId;
        }
        if (description != null) {
            this.description = description;
        }
        if (tags != null) {
            this.tags = tags;
        }
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }
        this.stockQuantity -= quantity;
    }
}
