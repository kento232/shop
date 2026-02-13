package jp.ken.shop.presentation.form;

import java.io.Serializable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CartForm implements Serializable {
 @NotBlank
 private String productId;
 
 @Min(1)
 private int qty =1;
}
