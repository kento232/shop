package jp.ken.shop.domain.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;



@Repository
public class CartRepository {

    private final JdbcTemplate jdbcTemplate;

    public CartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    /** LIKE のワイルドカードをエスケープ（\ % _） */
    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
    public List<Map<String, Object>> findByKeyword(String keyword, int limit) {
        String k = (keyword == null) ? "" : keyword.trim();
        if (!StringUtils.hasText(k)) {
            return List.of();
        }

        // LIKE のワイルドカードをエスケープ（\ % _）
        String pattern = "%" + escapeLike(k) + "%";

        String sql = """
            SELECT
                p.product_id                         AS product_id,
                p.product_name                       AS product_name,
                p.category_id                        AS category_id,
                p.subcategory_id                     AS subcategory_id,
                p.product_price                      AS product_price,
                COALESCE(p.product_discount_price,0) AS product_discount_price,
                p.product_image                      AS product_image,
                p.product_size                       AS product_size,
                p.product_color                      AS product_color,
                p.product_material                   AS product_material,
                p.product_weight                     AS product_weight,
                p.product_packingsize                AS product_packingsize,
                p.product_manufacture                AS product_manufacture,
                p.product_sale_startday              AS product_sale_startday,
                p.product_sale_stopday               AS product_sale_stopday,
                p.comment                            AS comment,
                p.valid_flag                         AS valid_flag
            FROM kenfurni_database.m_product p
            WHERE
                p.valid_flag = '0'
                AND (p.product_sale_startday IS NULL OR CURRENT_DATE >= p.product_sale_startday)
                AND (p.product_sale_stopday  IS NULL OR CURRENT_DATE <= p.product_sale_stopday)
                AND (CONCAT_WS(' ', p.product_name, p.comment) LIKE ?)
            ORDER BY p.product_id ASC
            """ + " LIMIT " + Math.max(1, Math.min(limit, 50)); // 50 に制限

        return jdbcTemplate.query(
            sql,
            ps -> ps.setString(1, pattern),
            new ColumnMapRowMapper()
        );
    }

    public List<Map<String, Object>> findAll(int limit) {
        String sql = """
            SELECT
                p.product_id                         AS product_id,
                p.product_name                       AS product_name,
                p.category_id                        AS category_id,
                p.subcategory_id                     AS subcategory_id,
                p.product_price                      AS product_price,
                COALESCE(p.product_discount_price,0) AS product_discount_price,
                p.product_image                      AS product_image,
                p.product_size                       AS product_size,
                p.product_color                      AS product_color,
                p.product_material                   AS product_material,
                p.product_weight                     AS product_weight,
                p.product_packingsize                AS product_packingsize,
                p.product_manufacture                AS product_manufacture,
                p.product_sale_startday              AS product_sale_startday,
                p.product_sale_stopday               AS product_sale_stopday,
                p.comment                            AS comment,
                p.valid_flag                         AS valid_flag
            FROM kenfurni_database.m_product p
            WHERE
                p.valid_flag = '0'
                AND (p.product_sale_startday IS NULL OR CURRENT_DATE >= p.product_sale_startday)
                AND (p.product_sale_stopday  IS NULL OR CURRENT_DATE <= p.product_sale_stopday)
            ORDER BY p.product_id ASC
            """ + " LIMIT " + Math.max(1, Math.min(limit, 200));

        return jdbcTemplate.query(sql, new ColumnMapRowMapper());
    }

   
}