package com.ecommerce.product.ai;

import java.util.Map;

public interface GeminiInteractionGateway {

    GeminiTextResult generateText(
            String model,
            String systemInstruction,
            String prompt,
            String previousInteractionId);

    String generateStructuredJson(
            String model,
            String systemInstruction,
            String prompt,
            Map<String, Object> jsonSchema);

    record GeminiTextResult(String text, String interactionId) {
    }
}
