package jp.ken.shop.domain.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PurchaseDetailRepository {

	private final JdbcTemplate jdbcTemplate;

	public PurchaseDetailRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void insert(
			int purchaseId,
			int purchaseIdNumber,
			String productId,
			int productPurchasePrice,
			int purchaseQuantity) {
		String sql = """
				INSERT INTO t_member_history_product_list
				  (purchase_id, purchase_id_number, product_id, product_purchase_price, purchase_quantity)
				VALUES (?, ?, ?, ?, ?)
				""";

		jdbcTemplate.update(sql,
				purchaseId,
				purchaseIdNumber,
				productId,
				productPurchasePrice,
				purchaseQuantity);
	}
}
