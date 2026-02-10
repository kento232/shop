package jp.ken.shop.presentation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;



		@Controller
		public class TopController {

		    private final JdbcTemplate jdbc;

		    public TopController(JdbcTemplate jdbc) {
		        this.jdbc = jdbc;
		    }

		    @GetMapping("/top")
		    public String top(Model model) {

		        String sql = """
		            SELECT product_id,
		                   product_name,
		                   product_price,
		                   product_discount_price,
		                   product_image
		            FROM kenfurni_database.m_product
		            WHERE valid_flag = '0'
		            ORDER BY product_id
		            """;

		        // ★ DBの全商品を List<Map<String,Object>> で取得
		        List<Map<String, Object>> products = jdbc.queryForList(sql);

		        // Thymeleafに渡す
		        model.addAttribute("products", products);

		        return "top";  // ← top.html にレンダリング
		    }
		}