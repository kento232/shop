package jp.ken.shop.domain.entity;

import lombok.Data;

@Data
public class CartItemEntity {

	private String productId;
	private String productName;
	private int price;
	private String productImage;
	private int qty;
	
	public int getSubtotal() {
		return price * qty;
	}
}
