package jp.ken.shop.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import jp.ken.shop.application.service.UserService;
import jp.ken.shop.domain.entity.RegisterEntity;
import jp.ken.shop.presentation.form.UserForm;

@Controller
@RequestMapping("/shop")
public class JdbcController {
	
	private final UserService userService;
	
	public JdbcController(UserService userService) {
		this.userService = userService;
	}
	@GetMapping 
	public String toTop(Model model) {
		model.addAttribute("userForm", new UserForm());
		return "top";
	}
	
	@GetMapping("/registerform")
	public String toForm(Model model) {
	    model.addAttribute("userForm", new UserForm());
	    return "memberRegist";
	}

	@PostMapping("/register")
	public String register(
	        @Validated @ModelAttribute("userForm") UserForm form,
	        BindingResult result,
	        Model model,
	        HttpSession session) {

	    if (!form.getPassword().equals(form.getConfirmPassword())) {
	        result.rejectValue("confirmPassword", "password.mismatch", "パスワードが一致しません。");
	    }

	    if (result.hasErrors()) {
	        return "memberRegist";
	    }

	    // ★ RegisterEntity を受け取る
	    RegisterEntity user = userService.register(form);

	    // ★ confirm に渡す
	    model.addAttribute("user", user);
	    session.setAttribute("loginUser", user);
	    return "confirm";
	}

}
