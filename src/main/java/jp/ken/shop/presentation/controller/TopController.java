package jp.ken.shop.presentation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import jp.ken.shop.application.service.CartSearchService;
import jp.ken.shop.application.service.ProductService;
import jp.ken.shop.presentation.form.SearchForm;

@Controller
public class TopController {

    private static final int INITIAL_LIMIT = 28; // 初期表示の件数
    private final CartSearchService cartSearchService;
    private final JdbcTemplate jdbc; // 初期表示（全件系）にだけ使う
    private final ProductService productService;


    public TopController(CartSearchService cartSearchService, JdbcTemplate jdbc,ProductService productService) {
        this.cartSearchService = cartSearchService;
        this.jdbc = jdbc;
        this.productService=productService;
    }

    /** どのハンドラでも必ず form をモデルに供給（Thymeleafの th:object 対策） */
    @ModelAttribute("form")
    public SearchForm setUpForm() {
        return new SearchForm();
    }

    /** トップ＋検索（/ と /top の両方を受ける） */
    @GetMapping({"/", "/top"})
    public String top(@ModelAttribute("form") SearchForm form, Model model) {

        List<Map<String, Object>> products;

        if (StringUtils.hasText(form.getKeyword())) {
            // キーワード検索（サービス側は空文字のとき List.of() を返す実装のままでOK）
            products = cartSearchService.search(form.getKeyword().trim());

            if (products == null || products.isEmpty()) {
                model.addAttribute("noResultMessage", "該当する商品がありません。");
            }
        } else {
            // 初回表示：公開中商品の新しい順（必要なら ORDER BY/件数は調整）
            String sql = """
                SELECT
                    p.product_id,
                    p.product_name,
                    p.product_price,
                    COALESCE(p.product_discount_price, 0) AS product_discount_price,
                    p.product_image
                FROM kenfurni_database.m_product p
                WHERE p.valid_flag = '0'
                  AND (p.product_sale_startday IS NULL OR CURRENT_DATE >= p.product_sale_startday)
                  AND (p.product_sale_stopday  IS NULL OR CURRENT_DATE  < p.product_sale_stopday)
                ORDER BY p.product_id DESC
                LIMIT ?
                """;
            products = jdbc.queryForList(sql, INITIAL_LIMIT);
        }

        model.addAttribute("products", products);
        return "top";
    }

   
     

/** 商品詳細（これだけあればOK） */
    @GetMapping("/products/{productId}")
    public String detail(@PathVariable String productId, Model model) {
        var product = productService.getDetailOrThrow(productId); // 見つからなければ 404
        model.addAttribute("product", product);
        return "products"; // product.html（詳細テンプレ）
    }

    /** 当面、/products への直アクセスはトップへ逃がす（エラー回避用） */
    @GetMapping("/products")
    public String productsPlaceholder() {
        return "redirect:/top";
    }
}
