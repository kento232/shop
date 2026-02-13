package jp.ken.shop.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.ken.shop.application.service.CartService;
import jp.ken.shop.domain.entity.CartEntity;
import jp.ken.shop.presentation.form.CartForm;

@Controller
@RequestMapping("/cart")
class CartController {
	
	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping
	
	public String view(HttpSession session, Model model) {
		
		CartEntity cart = cartService.getOrCreate(session);
		
		model.addAttribute("items", cart.getItems());
		model.addAttribute("cartCount", cart.getCount());
		model.addAttribute("subtotal", cart.getTotal());
		model.addAttribute("shipping", 0);
		model.addAttribute("total", cart.getTotal());
		
		return "cart";
	}
	
	@PostMapping ("/add")
	public String add(@Valid @ModelAttribute CartForm cartForm,
			BindingResult result,
			HttpSession session) {
		
		if (result.hasErrors()) {
			return "redirect:/cart";
		}
		
		cartService.add(session, cartForm.getProductId(), cartForm.getQty());
		return "redirect:/cart";
	}
	
	 @PostMapping("/delete")
	    public String delete(@RequestParam String productId, HttpSession session) {
	        cartService.deleteItem(session, productId);
	        return "redirect:/cart";
	}

}
