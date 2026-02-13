package jp.ken.shop.application.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jp.ken.shop.domain.entity.ProductEntity;
import jp.ken.shop.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductEntity getDetailOrThrow(String productId) {
        return productRepository.findPublishedById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
    }

    public List<ProductEntity> getAllPublished() {
        return productRepository.findAllPublished();
    }

    public List<ProductEntity> search(String q, Integer categoryId, int page, int size) {
        int pageIndex = Math.max(page - 1, 0);
        int limit = size;
        int offset = pageIndex * size;
        return productRepository.searchPublished(q, categoryId, limit, offset);
    }

    public int count(String q, Integer categoryId) {
        return productRepository.countPublished(q, categoryId);
    }
}