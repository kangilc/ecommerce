package com.ecommerce.product.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String name;
    private Long price;
    private Integer stockQuantity;
    private Long sellerId;
    private String description;
    private List<String> tags;
}
