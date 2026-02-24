package jp.ken.shop.presentation.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jp.ken.shop.presentation.form.LoginForm;

@Controller
public class LoginController {

	@GetMapping("/login")
	public String showLoginForm(Model model) {
		model.addAttribute("loginForm", new LoginForm());
		return "login";  // login.html を表示
	}
	
	@PostMapping("/login")
	public String login(
			@Validated @ModelAttribute ("loginForm")LoginForm form,
			BindingResult bindingResult,Model model) {



		// どちらかが空なら共通エラー 
		if (bindingResult.hasErrors() || form.getLoginInput() == null || form.getLoginInput().isBlank() || 
				form.getMemberPassword() == null || form.getMemberPassword().isBlank()) { 
			model.addAttribute("validationError", "会員ID、もしくはメールアドレス、パスワードは必須入力です。"); 
			
			return "login";
		}
		// バリデーションOKなら Spring Security に認証を任せる
		return "forward:/perform_login";

	}

}
