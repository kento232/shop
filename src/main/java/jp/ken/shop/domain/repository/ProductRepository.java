package jp.ken.shop.domain.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import jp.ken.shop.domain.entity.ProductEntity;
import jp.ken.shop.infrastructure.mapper.ProductRowMapper;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

	private final JdbcTemplate jdbcTemplate;
	private final ProductRowMapper rowMapper = new ProductRowMapper();

	/**
	 * 公開条件を満たす単品取得（商品詳細用）
	 */
	public Optional<ProductEntity> findPublishedById(String productId) {
		LocalDate today = LocalDate.now();
		String sql = """
				    SELECT *
				    FROM kenfurni_database.m_product
				    WHERE product_id = ?
				      AND valid_flag = 0
				      AND (product_sale_startday IS NULL OR product_sale_startday <= ?)
				      AND (product_sale_stopday  IS NULL OR product_sale_stopday > ?)
				""";
		List<ProductEntity> list = jdbcTemplate.query(
				sql,
				rowMapper,
				productId,
				java.sql.Date.valueOf(today),
				java.sql.Date.valueOf(today));
		return list.stream().findFirst();
	}

	/**
	 * 公開中の全商品（一覧向け、名称順）
	 */
	public List<ProductEntity> findAllPublished() {
		LocalDate today = LocalDate.now();
		String sql = """
				    SELECT *
				    FROM kenfurni_database.m_product
				    WHERE valid_flag = 0
				      AND (product_sale_startday IS NULL OR product_sale_startday <= ?)
				      AND (product_sale_stopday  IS NULL OR product_sale_stopday > ?)
				    ORDER BY product_name ASC
				""";
		return jdbcTemplate.query(
				sql,
				rowMapper,
				java.sql.Date.valueOf(today),
				java.sql.Date.valueOf(today));
	}

	/**
	 * キーワード・カテゴリ検索 + ページング
	 */
	public List<ProductEntity> searchPublished(String q, Integer categoryId, int limit, int offset) {
		LocalDate today = LocalDate.now();
		StringBuilder sql = new StringBuilder("""
				    SELECT *
				    FROM m_product
				    WHERE valid_flag = 0
				      AND (product_sale_startday IS NULL OR product_sale_startday <= ?)
				      AND (product_sale_stopday  IS NULL OR product_sale_stopday > ?)
				""");
		List<Object> params = new ArrayList<>();
		params.add(java.sql.Date.valueOf(today));
		params.add(java.sql.Date.valueOf(today));

		if (StringUtils.hasText(q)) {
			sql.append(" AND product_name LIKE ? ");
			params.add("%" + q.trim() + "%");
		}
		if (categoryId != null) {
			sql.append(" AND category_id = ? ");
			params.add(categoryId);
		}

		sql.append(" ORDER BY product_name ASC LIMIT ? OFFSET ? ");
		params.add(limit);
		params.add(offset);

		return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
	}

	/**
	 * 検索件数（ページング用）
	 */
	public int countPublished(String q, Integer categoryId) {
		LocalDate today = LocalDate.now();
		StringBuilder sql = new StringBuilder("""
				    SELECT COUNT(*)
				    FROM m_product
				    WHERE valid_flag = 0
				      AND (product_sale_startday IS NULL OR product_sale_startday <= ?)
				      AND (product_sale_stopday  IS NULL OR product_sale_stopday > ?)
				""");
		List<Object> params = new ArrayList<>();
		params.add(java.sql.Date.valueOf(today));
		params.add(java.sql.Date.valueOf(today));

		if (StringUtils.hasText(q)) {
			sql.append(" AND product_name LIKE ? ");
			params.add("%" + q.trim() + "%");
		}
		if (categoryId != null) {
			sql.append(" AND category_id = ? ");
			params.add(categoryId);
		}

		Integer total = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
		return total != null ? total : 0;
	}

	/**
	    * 大分類名を valid_flag=0 のときだけ返す
	    */
	public Optional<String> findCategoryNameIfValid(int categoryId) {
		List<String> list = jdbcTemplate.query(
				"SELECT category_name FROM m_category WHERE category_id = ? AND valid_flag = 0",
				(rs, i) -> rs.getString("category_name"),
				categoryId);
		return list.stream().findFirst();
	}

	public Optional<String> findSubcategoryNameIfValid(int categoryId, int subcategoryId) {
		List<String> list = jdbcTemplate.query(
				"SELECT subcategory_name FROM m_subcategory " +
						"WHERE category_id = ? AND subcategory_id = ? AND valid_flag = 0",
				(rs, i) -> rs.getString("subcategory_name"),
				categoryId, subcategoryId);
		return list.stream().findFirst();
	}

}
