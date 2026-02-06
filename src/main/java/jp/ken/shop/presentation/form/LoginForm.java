package jp.ken.shop.presentation.form;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginForm implements Serializable{
	
	@NotBlank(message = "")
	private String loginInput;
	
	@NotBlank(message = "")
	private String memberPassword;

}
