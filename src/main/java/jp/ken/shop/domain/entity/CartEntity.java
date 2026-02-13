package jp.ken.shop.domain.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CartEntity {

    private List<CartItemEntity> items = new ArrayList<>();

    public int getCount() {
        return items.stream().mapToInt(CartItemEntity::getQty).sum();
    }

    public int getTotal() {
        return items.stream().mapToInt(CartItemEntity::getSubtotal).sum();
    }
}
