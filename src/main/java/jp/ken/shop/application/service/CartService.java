package jp.ken.shop.application.service;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import jp.ken.shop.domain.entity.CartEntity;
import jp.ken.shop.domain.entity.CartItemEntity;
import jp.ken.shop.domain.entity.ProductEntity;
import jp.ken.shop.domain.repository.CartRepository;

@Service
public class CartService {

	public static final String SESSION_KEY = "cart";

	public final CartRepository cartRepository;
	

	public CartService(CartRepository cartRepository) {
		this.cartRepository = cartRepository;
	}
	public void deleteItem(HttpSession session, String productId) {
	    CartEntity cart = getOrCreate(session);
	    cart.getItems().removeIf(it -> it.getProductId().equals(productId));
	}


	public CartEntity getOrCreate(HttpSession session) {
		CartEntity cart = (CartEntity) session.getAttribute(SESSION_KEY);
		if (cart == null) {
			cart = new CartEntity();
			session.setAttribute(SESSION_KEY, cart);
		}
		return cart;
	}

	public void add(HttpSession session, String productId, int qty) {

		CartEntity cart = getOrCreate(session);

		for (CartItemEntity item : cart.getItems()) {
			if (item.getProductId().equals(productId)) {
				item.setQty(item.getQty() + qty);
				return;
			}
		}

		ProductEntity product = cartRepository.findById(productId);

		if (product == null) {
			throw new IllegalArgumentException("商品が存在しません：　" + productId);
		}
		CartItemEntity item = new CartItemEntity();

		item.setProductId(product.getProductId());
		item.setProductName(product.getProductName());
		item.setPrice(product.getProductPrice());
		item.setProductImage(product.getProductImage());
		item.setQty(qty);

		cart.getItems().add(item);
	}
	


}
