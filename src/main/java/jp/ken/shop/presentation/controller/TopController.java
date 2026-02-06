package jp.ken.shop.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

	@Controller
	public class TopController {

		@GetMapping("/top")
		public String top() {
			return "top"; // → src/main/resources/templates/top.html
    }
}