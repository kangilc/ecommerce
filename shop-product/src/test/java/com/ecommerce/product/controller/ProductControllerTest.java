package com.ecommerce.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.product.ai.ProductAiService;
import com.ecommerce.product.config.WebConfig;
import com.ecommerce.product.dto.ProductCopyResponse;
import com.ecommerce.product.dto.ProductQaResponse;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import(WebConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductAiService productAiService;

    @Test
    void findAll_returnsWrappedApiResponse() throws Exception {
        when(productService.findAll()).thenReturn(List.of(
                ProductResponse.builder()
                        .id(1L)
                        .name("에어팟 케이스")
                        .price(19000L)
                        .stockQuantity(30)
                        .tags(List.of("케이스", "악세서리"))
                        .build()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("에어팟 케이스"))
                .andExpect(jsonPath("$.data[0].tags[0]").value("케이스"));
    }

    @Test
    void generateProductCopy_returnsGeneratedCopy() throws Exception {
        when(productAiService.generateProductCopy(any())).thenReturn(
                ProductCopyResponse.builder()
                        .description("고급 실리콘 소재의 보호 케이스")
                        .highlights(List.of("충격 흡수", "가벼운 무게"))
                        .tags(List.of("케이스", "실리콘"))
                        .build());

        mockMvc.perform(post("/api/products/ai/generate-copy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"에어팟 케이스","price":19000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("고급 실리콘 소재의 보호 케이스"))
                .andExpect(jsonPath("$.data.tags[1]").value("실리콘"));
    }

    @Test
    void askAboutProduct_returnsAnswerAndInteractionId() throws Exception {
        when(productAiService.answerProductQuestion(eq(1L), any())).thenReturn(
                ProductQaResponse.builder()
                        .answer("현재 재고 30개 보유 중입니다.")
                        .interactionId("inter-001")
                        .build());

        mockMvc.perform(post("/api/products/1/ai/qa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"재고 있나요?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.answer").value("현재 재고 30개 보유 중입니다."))
                .andExpect(jsonPath("$.data.interactionId").value("inter-001"));
    }
}
