package jp.ken.shop.domain.entity;

import lombok.Data;

@Data

public class ProductEntity {

	private String productId;
	private String productName;
	
	private int productPrice;
	
	private String productImage;
	
}
