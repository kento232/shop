package jp.ken.shop.domain.entity;

import java.time.LocalDate;

import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Table(name = "m_product")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

	private String productId;
	private String productName;
	
	private int productPrice;
	
	private String productImage;
	
	 
	

	 
	 private Integer categoryId;

	 
	 private Integer subcategoryId;


	

	 
	 private Integer productDiscountPrice;

	 
	

	 
	 private String productSize;

	 private String productColor;

	 
	 private String productMaterial;


	 private String productWeight;

	 
	 private String productPackingSize;

	 
	 private String productManufacture;

	 
	 private LocalDate productSaleStartDay;

	 
	 private LocalDate productSaleStopDay;

	 
	 private String comment;

	 
	 private Integer validFlag;

	}

