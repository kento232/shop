package jp.ken.shop.presentation.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductSearchRestController {

	private final JdbcTemplate jdbc;
	public ProductSearchRestController(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
		}
	
	@GetMapping("/api/products/search")
	public Map<String, Object> search(
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size) {
		
		int limit = Math.max(1, Math.min(size, 100)); // 上限100
		int offset = (Math.max(page, 1) - 1) * limit;
		StringBuilder sql = new StringBuilder();
		
		sql.append(
            "SELECT " +
            "  p.product_id, " +
            "  p.product_name, " +
            "  p.product_price, " +
            "  COALESCE(p.product_discount_price, 0) AS product_discount_price, " +
            "  p.product_image " +
            "FROM kenfurni_database.m_product p " +
            "WHERE p.valid_flag = '0' " +
            "  AND (p.product_sale_startday IS NULL OR CURRENT_DATE >= p.product_sale_startday) " +
            "  AND (p.product_sale_stopday IS NULL OR CURRENT_DATE < p.product_sale_stopday) "
        );

        List<Object> params = new ArrayList<>();

        // ▼ カテゴリ条件（※ 実スキーマに合わせて p.category_id を調整）
        if (categoryId != null) {
            sql.append(" AND p.category_id = ? ");
            params.add(categoryId);
        }

        // ▼ キーワード条件（商品名 + 説明カラムがあれば OR 追加）
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (p.product_name LIKE ? ");
            params.add(like);

            // 説明カラムが存在する場合のみ有効化してください（無ければ次の2行を削除）
            sql.append(" OR p.product_description LIKE ? ");
            params.add(like);

            sql.append(") ");
        }

        sql.append(" ORDER BY p.product_id DESC ");
        // hasNext 判定のために size+1 で取得
        sql.append(" LIMIT ? OFFSET ? ");
        params.add(limit + 1);
        params.add(offset);

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params.toArray());

        boolean hasNext = rows.size() > limit;
        if (hasNext) {
            rows = rows.subList(0, limit);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", rows);
        result.put("page", Math.max(page, 1));
        result.put("size", limit);
        result.put("hasNext", hasNext);
        return result;
    }
}