package jp.ken.shop.presentation.form;

import java.io.Serializable;

import lombok.Data;

@Data
public class LoginForm implements Serializable{
	
	
	private String loginInput;
	
	
	private String memberPassword;

}
