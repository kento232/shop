package jp.ken.shop.presentation.api;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryRestController {
	private final JdbcTemplate jdbc;

	public CategoryRestController(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	/** 有効カテゴリ一覧（valid_flag=0） */

	@GetMapping("/api/categories")
	public List<Map<String, Object>> list() {
		String sql = "SELECT category_id AS id, category_name AS name " +
				"FROM m_category " +
				"WHERE valid_flag = 0 " +
				"ORDER BY category_id";
		return jdbc.queryForList(sql);
	}

	
}