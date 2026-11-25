package com.ecommerce.product.ai;

import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.product.dto.ProductCopyRequest;
import com.ecommerce.product.dto.ProductCopyResponse;
import com.ecommerce.product.dto.ProductQaRequest;
import com.ecommerce.product.dto.ProductQaResponse;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides Gemini-powered product copywriting and interactive Q&A features.
 */
@Service
@RequiredArgsConstructor
public class ProductAiService {

    private static final String COPY_SYSTEM_INSTRUCTION = """
            You are an e-commerce merchandising copywriter.
            Write clear, accurate, persuasive Korean product descriptions and search tags based only on the provided product metadata.
            Do not invent unsupported technical specs, warranties, or discounts.
            """;

    private static final String QA_SYSTEM_INSTRUCTION = """
            You are a helpful e-commerce shopping assistant.
            Answer the customer's question concisely in Korean using the provided product information.
            If a detail is not present in the product context, explicitly say it is not specified rather than guessing.
            """;

    private final GeminiInteractionGateway geminiGateway;
    private final GeminiProperties properties;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public ProductCopyResponse generateProductCopy(ProductCopyRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "Product name is required");
        }

        String prompt = buildCopyPrompt(request);
        String rawJson = geminiGateway.generateStructuredJson(
                properties.getModel(),
                COPY_SYSTEM_INSTRUCTION,
                prompt,
                buildCopyResponseSchema());

        return parseCopyResponse(rawJson);
    }

    @Transactional(readOnly = true)
    public ProductQaResponse answerProductQuestion(Long productId, ProductQaRequest request) {
        validateQuestion(request);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "Product not found"));

        ProductContext context = new ProductContext(
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getDescription(),
                ProductResponse.parseTags(product.getTags()));

        return askWithContext(context, request.getQuestion(), request.getPreviousInteractionId());
    }

    public ProductQaResponse answerProductQuestion(ProductQaRequest request) {
        validateQuestion(request);
        if (request.getProductName() == null || request.getProductName().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "Product name is required");
        }

        ProductContext context = new ProductContext(
                request.getProductName(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getDescription(),
                request.getTags() != null ? request.getTags() : List.of());

        return askWithContext(context, request.getQuestion(), request.getPreviousInteractionId());
    }

    private ProductQaResponse askWithContext(
            ProductContext context,
            String question,
            String previousInteractionId) {
        String prompt = """
                [상품 정보]
                - 상품명: %s
                - 가격: %s
                - 재고 수량: %s
                - 상품 설명: %s
                - 태그: %s

                [고객 질문]
                %s
                """.formatted(
                context.name(),
                context.price() != null ? context.price() + "원" : "미정",
                context.stockQuantity() != null ? context.stockQuantity() + "개" : "미정",
                hasText(context.description()) ? context.description() : "등록된 설명 없음",
                context.tags() != null && !context.tags().isEmpty() ? String.join(", ", context.tags()) : "없음",
                question.trim());

        GeminiInteractionGateway.GeminiTextResult result = geminiGateway.generateText(
                properties.getModel(),
                QA_SYSTEM_INSTRUCTION,
                prompt,
                previousInteractionId);

        return ProductQaResponse.builder()
                .answer(result.text())
                .interactionId(result.interactionId())
                .build();
    }

    private void validateQuestion(ProductQaRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "Question is required");
        }
    }

    private String buildCopyPrompt(ProductCopyRequest request) {
        String tone = hasText(request.getTone()) ? request.getTone().trim() : "신뢰감 있고 친절한";
        String keywords = hasText(request.getKeywords()) ? request.getKeywords().trim() : "없음";
        String priceText = request.getPrice() != null ? request.getPrice() + "원" : "미정";

        return """
                다음 상품 정보를 바탕으로 한국어 이커머스 상품 상세 설명, 핵심 포인트 3개, 검색용 태그 5개를 생성해 주세요.
                - 상품명: %s
                - 판매가: %s
                - 강조 키워드: %s
                - 톤앤매너: %s
                """.formatted(request.getName().trim(), priceText, keywords, tone);
    }

    private Map<String, Object> buildCopyResponseSchema() {
        Map<String, Object> stringSchema = Map.of("type", "string");
        Map<String, Object> stringArraySchema = Map.of(
                "type", "array",
                "items", stringSchema);

        Map<String, Object> propertiesMap = new LinkedHashMap<>();
        propertiesMap.put("description", Map.of(
                "type", "string",
                "description", "3~5문장 분량의 매력적인 한국어 상품 상세 설명"));
        propertiesMap.put("highlights", stringArraySchema);
        propertiesMap.put("tags", stringArraySchema);

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", propertiesMap);
        schema.put("required", List.of("description", "highlights", "tags"));
        return schema;
    }

    private ProductCopyResponse parseCopyResponse(String rawJson) {
        String normalized = stripJsonFence(rawJson);
        try {
            ProductCopyResponse parsed = objectMapper.readValue(normalized, ProductCopyResponse.class);
            return ProductCopyResponse.builder()
                    .description(parsed.getDescription() != null ? parsed.getDescription().trim() : "")
                    .highlights(parsed.getHighlights() != null ? parsed.getHighlights() : List.of())
                    .tags(parsed.getTags() != null ? parsed.getTags() : List.of())
                    .build();
        } catch (JsonProcessingException e) {
            throw new CustomException(
                    ErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse Gemini structured response");
        }
    }

    private String stripJsonFence(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ProductContext(
            String name,
            Long price,
            Integer stockQuantity,
            String description,
            List<String> tags) {
    }
}
