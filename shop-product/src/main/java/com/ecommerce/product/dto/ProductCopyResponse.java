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
public class ProductCopyResponse {
    private String description;
    private List<String> highlights;
    private List<String> tags;
}
