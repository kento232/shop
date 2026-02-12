package jp.ken.shop.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jp.ken.shop.domain.repository.CartRepository;


@Service
public class CartSearchService { 
    private static final int DEFAULT_LIMIT = 50;
    private final CartRepository cartRepository;

    public CartSearchService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public List<Map<String, Object>> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();
        return cartRepository.findByKeyword(keyword.trim(), DEFAULT_LIMIT);
    }
}

