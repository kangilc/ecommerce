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
public class ProductQaRequest {
    private String question;
    private String previousInteractionId;
    private String productName;
    private Long price;
    private Integer stockQuantity;
    private String description;
    private List<String> tags;
}
