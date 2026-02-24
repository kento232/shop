package jp.ken.shop.presentation.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jp.ken.shop.application.service.UserService;
import jp.ken.shop.common.validator.groups.ValidGroupOrder;
import jp.ken.shop.domain.entity.RegisterEntity;
import jp.ken.shop.presentation.form.UserForm;

@Controller
@RequestMapping("/shop")
public class JdbcController {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;

	public JdbcController(
			UserService userService,
			AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
	}

	@GetMapping("/confirm")
	public String confirmPage(HttpSession session, Model model) {
	    RegisterEntity user = (RegisterEntity) session.getAttribute("loginUser");
	    UserForm form = (UserForm) session.getAttribute("userForm");

	    model.addAttribute("user", user);
	    model.addAttribute("userForm", form);
	    return "confirm";
	}




	@GetMapping("/registerform")
	public String toForm(Model model) {
		model.addAttribute("userForm", new UserForm());
		return "memberRegist";
	}

	@PostMapping("/register")
	public String register(
			@Validated(ValidGroupOrder.class) @ModelAttribute("userForm") UserForm form,
			BindingResult result,
			Model model,
			HttpSession session,
			HttpServletRequest request,
			HttpServletResponse response) {
		if (!form.getPassword().equals(form.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "password.mismatch", "パスワードが一致しません。");
		}
		if (userService.existsByEmail(form.getEmail())) {
			result.rejectValue("email","email.exists", "このメールアドレスはすでに登録されています。");
		}
		if (result.hasErrors()) {
			return "memberRegist";
		}

		// 1) DB登録（パスワードはBCryptで保存されている前提）
		RegisterEntity user = userService.register(form);
		form.setLoginInput(form.getEmail());

		// 2) 自動ログイン（★ここが要点）
		UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(form.getLoginInput(),
				form.getPassword());

		Authentication auth = authenticationManager.authenticate(authReq);
		SecurityContextHolder.getContext().setAuthentication(auth);

		// 3) ★リダイレクト後もログイン状態を残すために保存（Spring Security 6で重要）
		securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

		// 必要なら独自sessionも（Spring Securityのログインとは別）
		session.setAttribute("loginUser", user);
		session.setAttribute("userForm", form);
		session.setAttribute("memberId", user.getMemberId());
		// 遷移先は好きに
		return "redirect:/shop/confirm";
		// return "redirect:/shop/confirm"; でもOK
	}

}
