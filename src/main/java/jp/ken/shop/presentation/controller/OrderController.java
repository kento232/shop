package jp.ken.shop.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jp.ken.shop.application.service.CartService;
import jp.ken.shop.application.service.OrderService;
import jp.ken.shop.application.service.UserSearchService;
import jp.ken.shop.domain.entity.CartEntity;
@RequestMapping("/order")
@Controller
public class OrderController {

	private final CartService cartService;
	private final OrderService orderService;
    private final UserSearchService userSearchService;
    
	public OrderController(CartService cartService,
			OrderService orderService, UserSearchService userSearchService) {
		this.cartService = cartService;
		this.orderService = orderService;
		this.userSearchService = userSearchService;
	}

	/**
	 * 🧾 購入確認画面
	 * GET /order/confirm
	 */
	@GetMapping("/confirm")
	public String confirm(HttpSession session, Model model) {

		CartEntity cart = cartService.getOrCreate(session);

		if (cart.getItems().isEmpty()) {
			return "redirect:/cart"; // カート空なら戻す
		}

		int totalAmount = calcTotal(cart);

		model.addAttribute("cartItems", cart.getItems());
		model.addAttribute("totalAmount", totalAmount);

		// ポイント未実装：全部0
		model.addAttribute("earnedPoint", 0);
		model.addAttribute("usePoint", 0);
		model.addAttribute("discount", 0);
		model.addAttribute("billedAmount", totalAmount);

		return "purchaseConfirm";
	}

	/**
	 * 🧾 注文確定（DB保存）
	 * POST /order/submit
	 */
	@PostMapping("/submit")
	public String submit(HttpSession session,
	                     RedirectAttributes ra) {

	    Integer memberId = (Integer) session.getAttribute("memberId");

	    if (memberId == null) {
	        return "redirect:/login"; // 未ログイン
	    }

	    int purchaseId = orderService.createOrder(session, memberId);

	    String orderNo = orderService.formatOrderNo(purchaseId);
	    ra.addFlashAttribute("orderNo", orderNo);

	    return "redirect:/order/complete";
	}




	@GetMapping("/complete")
	public String complete(Model model) {
		return "order/purchaseCompletion";
	}

	private int calcTotal(CartEntity cart) {
		int sum = 0;
		for (var item : cart.getItems()) {
			sum += item.getPrice() * item.getQty();
		}
		return sum;
	}
	
}
