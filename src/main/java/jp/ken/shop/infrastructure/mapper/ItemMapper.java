package jp.ken.shop.infrastructure.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.data.repository.query.Param;


public interface ItemMapper {

    List<Map<String, Object>> searchProducts(@Param("keyword") String keyword,
                                             @Param("limit") int limit);
    Map<String, Object> findById(@Param("id") String productId);

    List<Map<String, Object>> findByIds(@Param("ids") List<String> productIds);
}
