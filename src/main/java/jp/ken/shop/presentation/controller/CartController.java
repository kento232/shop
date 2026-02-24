package jp.ken.shop.presentation.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

	@GetMapping("/api/count")
	@ResponseBody
	public Map<String, Integer> getCountInCartPrefix(HttpSession session) {
		CartEntity cart = cartService.getOrCreate(session);
		int count = Math.max(0, cart.getCount());
		return Collections.singletonMap("count", count);
	}

	@GetMapping

	public String view(HttpSession session, Model model) {

		CartEntity cart = cartService.getOrCreate(session);

		model.addAttribute("items", cart.getItems());
		model.addAttribute("cartCount", cart.getCount());
		model.addAttribute("subtotal", cart.getTotal());
		model.addAttribute("shipping", 0);
		model.addAttribute("total", cart.getTotal());
		Integer memberId = (Integer) session.getAttribute("memberId");
		if (memberId == null) {
			model.addAttribute("needLogin", true);
		}

		return "cart";
	}

	@PostMapping("/add")
	public String add(@Valid @ModelAttribute CartForm cartForm,
			BindingResult result,
			HttpSession session) {

		if (result.hasErrors()) {
			return "redirect:/cart";
		}

		cartService.add(session, cartForm.getProductId(), cartForm.getQty());

		CartEntity cart = cartService.getOrCreate(session);
		session.setAttribute("cartCount", cart.getCount());

		return "redirect:/cart";
	}

	@PostMapping("/delete")
	public String delete(@RequestParam String productId, HttpSession session) {
		cartService.deleteItem(session, productId);

		CartEntity cart = cartService.getOrCreate(session);
		session.setAttribute("cartCount", cart.getCount());

		return "redirect:/cart";
	}

	// リクエストDTO（数量のみ）gg
	static class UpdateQuantityRequest {
		private int quantity;

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}

	// レスポンスDTO（画面反映に必要な最低限の値）
	static class UpdateQuantityResponse {
		public int itemSubtotal;
		public int cartSubtotal;
		public int shipping;
		public int cartTotal;
		public int cartCount;

		public UpdateQuantityResponse(int itemSubtotal, int cartSubtotal, int shipping, int cartTotal, int cartCount) {
			this.itemSubtotal = itemSubtotal;
			this.cartSubtotal = cartSubtotal;
			this.shipping = shipping;

			this.cartTotal = cartTotal;
			this.cartCount = cartCount;
		}
	}

	/** AJAX用 数量更新（/cart/api/items/{productId}/quantity） */
	@PostMapping("/api/items/{productId}/quantity")
	@ResponseBody
	public ResponseEntity<?> updateQuantityApi(
			@PathVariable String productId,
			@RequestBody UpdateQuantityRequest req,
			HttpSession session) {

		try {
			cartService.updateQty(session, productId, req.getQuantity());

			CartEntity cart = cartService.getOrCreate(session);
			int cartSubtotal = cart.getTotal();
			int shipping = 0;
			int cartTotal = cartSubtotal + shipping;

			int cartCount = cart.getCount();

			// 該当行の小計（単価×数量）
			int itemSubtotal = cart.getItems().stream()
					.filter(i -> i.getProductId().equals(productId))
					.findFirst()
					.map(i -> i.getPrice() * i.getQty())
					.orElse(0);

			// ヘッダーのカート件数更新
			session.setAttribute("cartCount", cartCount);

			return ResponseEntity.ok(new UpdateQuantityResponse(
					itemSubtotal, cartSubtotal, shipping, cartTotal, cartCount));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		} catch (Exception ex) {
			return ResponseEntity.internalServerError().body("数量更新に失敗しました");
		}
	}
}
