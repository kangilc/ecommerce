package com.ecommerce.product.ai;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class GeminiProperties {

    private final String apiKey;
    private final String model;

    public GeminiProperties(
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-3.5-flash-lite}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }
}
