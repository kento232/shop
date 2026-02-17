package jp.ken.shop.application.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import jp.ken.shop.domain.entity.CartEntity;
import jp.ken.shop.domain.entity.CartItemEntity;
import jp.ken.shop.domain.repository.PurchaseDetailRepository;
import jp.ken.shop.domain.repository.PurchaseRepository;

@Service
public class OrderService {

    private final CartService cartService;
    private final PurchaseRepository purchaseRepository;               // ヘッダ用
    private final PurchaseDetailRepository purchaseDetailRepository;   // 明細用

    public OrderService(
            CartService cartService,
            PurchaseRepository purchaseRepository,
            PurchaseDetailRepository purchaseDetailRepository
    ) {
        this.cartService = cartService;
        this.purchaseRepository = purchaseRepository;
        this.purchaseDetailRepository = purchaseDetailRepository;
    }

    /**
     * 注文確定（DB保存）
     * 戻り値：purchase_id（完了画面の注文番号の元）
     */
  
    @Transactional
    public int createOrder(HttpSession session, int memberId) {

        CartEntity cart = cartService.getOrCreate(session);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("カートが空です");
        }

        // ★ アプリ側で注文番号を採番
        int purchaseId = purchaseRepository.nextPurchaseId();

        int lineNo = 1;

        for (CartItemEntity item : cart.getItems()) {

            purchaseRepository.insert(
                    purchaseId,
                    lineNo,
                    memberId,
                    LocalDate.now(),
                    item.getProductId(),
                    item.getQty(),
                    "1",     // 支払方法（例：1=クレカ）
                    "0",     // 通常配送
                    null,
                    null
            );

            lineNo++;
        }

        cart.getItems().clear();

        return purchaseId;
    }


    public String formatOrderNo(int purchaseId) {
        return String.format("%06d", purchaseId); // 0埋め6桁
    }

    private int calcTotalAmount(CartEntity cart) {
        int sum = 0;
        for (CartItemEntity item : cart.getItems()) {
            sum += item.getPrice() * item.getQty();
        }
        return sum;
    }
}
