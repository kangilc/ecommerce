package com.ecommerce.product.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.common.exception.CustomException;
import com.ecommerce.product.dto.ProductCopyRequest;
import com.ecommerce.product.dto.ProductCopyResponse;
import com.ecommerce.product.dto.ProductQaRequest;
import com.ecommerce.product.dto.ProductQaResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductAiServiceTest {

    @Mock
    private GeminiInteractionGateway geminiGateway;

    @Mock
    private ProductRepository productRepository;

    private ProductAiService productAiService;

    @BeforeEach
    void setUp() {
        GeminiProperties properties = new GeminiProperties("test-key", "gemini-4-hydrogen");
        productAiService = new ProductAiService(geminiGateway, properties, productRepository, new ObjectMapper());
    }

    @Test
    void generateProductCopy_parsesStructuredJsonResponse() {
        when(geminiGateway.generateStructuredJson(eq("gemini-4-hydrogen"), anyString(), anyString(), anyMap()))
                .thenReturn("""
                        ```json
                        {
                          "description": "가볍고 선명한 사운드를 제공하는 무선 이어폰입니다.",
                          "highlights": ["액티브 노이즈 캔슬링", "최대 24시간 재생", "생활 방수 지원"],
                          "tags": ["무선이어폰", "블루투스", "노이즈캔슬링"]
                        }
                        ```
                        """);

        ProductCopyResponse response = productAiService.generateProductCopy(
                ProductCopyRequest.builder()
                        .name("프로 사운드 무선 이어폰")
                        .price(129000L)
                        .keywords("노이즈캔슬링, 경량")
                        .build());

        assertThat(response.getDescription()).contains("무선 이어폰");
        assertThat(response.getHighlights()).hasSize(3);
        assertThat(response.getTags()).containsExactly("무선이어폰", "블루투스", "노이즈캔슬링");
    }

    @Test
    void generateProductCopy_throwsWhenNameBlank() {
        assertThatThrownBy(() -> productAiService.generateProductCopy(
                ProductCopyRequest.builder().name("  ").build()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("Product name is required");
    }

    @Test
    void answerProductQuestion_usesSavedProductAndPreviousInteractionId() {
        Product product = Product.builder()
                .name("기계식 키보드")
                .price(89000L)
                .stockQuantity(15)
                .description("저소음 적축 스위치가 탑재된 텐키리스 키보드")
                .tags("키보드,저소음적축")
                .build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(geminiGateway.generateText(eq("gemini-4-hydrogen"), anyString(), anyString(), eq("prev-123")))
                .thenReturn(new GeminiInteractionGateway.GeminiTextResult("현재 재고는 15개이며 가격은 89,000원입니다.", "next-456"));

        ProductQaResponse response = productAiService.answerProductQuestion(
                10L,
                ProductQaRequest.builder()
                        .question("재고 얼마나 남았나요?")
                        .previousInteractionId("prev-123")
                        .build());

        assertThat(response.getAnswer()).contains("15개");
        assertThat(response.getInteractionId()).isEqualTo("next-456");
        verify(geminiGateway).generateText(eq("gemini-4-hydrogen"), anyString(), anyString(), eq("prev-123"));
    }

    @Test
    void answerProductQuestion_withInlineContext_worksWithoutDbLookup() {
        when(geminiGateway.generateText(eq("gemini-4-hydrogen"), anyString(), anyString(), isNull()))
                .thenReturn(new GeminiInteractionGateway.GeminiTextResult("상품 설명 기준 무선 충전이 가능합니다.", "int-1"));

        ProductQaResponse response = productAiService.answerProductQuestion(
                ProductQaRequest.builder()
                        .productName("스마트 램프")
                        .description("무선 충전을 지원하는 데스크 램프")
                        .question("무선 충전 되나요?")
                        .build());

        assertThat(response.getAnswer()).contains("무선 충전");
        assertThat(response.getInteractionId()).isEqualTo("int-1");
    }
}
