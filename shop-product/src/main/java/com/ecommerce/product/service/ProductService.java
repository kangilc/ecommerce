package com.ecommerce.product.service;

import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = findEntityById(id);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "Product name is required");
        }
        Product product = Product.builder()
                .name(request.getName().trim())
                .price(request.getPrice() != null ? request.getPrice() : 0L)
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .sellerId(request.getSellerId())
                .description(request.getDescription())
                .tags(ProductResponse.joinTags(request.getTags()))
                .build();
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntityById(id);
        String joinedTags = request.getTags() != null ? ProductResponse.joinTags(request.getTags()) : null;
        product.update(
                request.getName(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getSellerId(),
                request.getDescription(),
                joinedTags);
        return ProductResponse.from(product);
    }

    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        log.info("Decrease stock: productId={}, quantity={}", productId, quantity);

        Product product = findEntityById(productId);
        product.decreaseStock(quantity);
    }

    private Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "Product not found"));
    }
}
