package com.ecommerce.product.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.product.ai.ProductAiService;
import com.ecommerce.product.dto.ProductCopyRequest;
import com.ecommerce.product.dto.ProductCopyResponse;
import com.ecommerce.product.dto.ProductQaRequest;
import com.ecommerce.product.dto.ProductQaResponse;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductAiService productAiService;

    @GetMapping
    public ApiResponse<List<ProductResponse>> findAll() {
        return ApiResponse.success(productService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(productService.findById(id));
    }

    @PostMapping
    public ApiResponse<ProductResponse> create(@RequestBody ProductRequest request) {
        return ApiResponse.success(productService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        return ApiResponse.success(productService.update(id, request));
    }

    @PostMapping("/ai/generate-copy")
    public ApiResponse<ProductCopyResponse> generateProductCopy(@RequestBody ProductCopyRequest request) {
        return ApiResponse.success(productAiService.generateProductCopy(request));
    }

    @PostMapping("/{id}/ai/qa")
    public ApiResponse<ProductQaResponse> askAboutProduct(
            @PathVariable Long id,
            @RequestBody ProductQaRequest request) {
        return ApiResponse.success(productAiService.answerProductQuestion(id, request));
    }

    @PostMapping("/ai/qa")
    public ApiResponse<ProductQaResponse> askWithInlineContext(@RequestBody ProductQaRequest request) {
        return ApiResponse.success(productAiService.answerProductQuestion(request));
    }
}
