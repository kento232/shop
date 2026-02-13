package jp.ken.shop.infrastructure.mapper;


import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import jp.ken.shop.domain.entity.ProductEntity;

public class ProductRowMapper implements RowMapper<ProductEntity> {

    @Override
    public ProductEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProductEntity p = new ProductEntity();

        // 文字列系
        p.setProductId(rs.getString("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setProductImage(rs.getString("product_image"));
        p.setProductSize(rs.getString("product_size"));
        p.setProductColor(rs.getString("product_color"));
        p.setProductMaterial(rs.getString("product_material"));
        p.setProductWeight(rs.getString("product_weight"));

        // ★列名は DB 実体に合わせる（アンダースコアなし：product_packingsize）
        p.setProductPackingSize(rs.getString("product_packingsize"));

        p.setProductManufacture(rs.getString("product_manufacture"));
        p.setComment(rs.getString("comment"));

        // 整数系（null/文字列/小数のどれでも安全に変換）
        p.setCategoryId(readInt(rs, "category_id"));
        p.setSubcategoryId(readInt(rs, "subcategory_id"));
        p.setProductPrice(readInt(rs, "product_price"));
        p.setProductDiscountPrice(readInt(rs, "product_discount_price"));
        p.setValidFlag(readInt(rs, "valid_flag"));

        // 日付系
        Date start = rs.getDate("product_sale_startday");
        Date stop  = rs.getDate("product_sale_stopday");
        p.setProductSaleStartDay(start == null ? null : start.toLocalDate());
        p.setProductSaleStopDay(stop  == null ? null : stop.toLocalDate());

        return p;
    }

    /**
     * 列値を安全に Integer へ変換するヘルパ。
     * - null → null
     * - Number（Integer/Long/BigDecimal…）→ intValue()
     * - String → 空文字は null、数値文字列は Integer.valueOf()
     * - その他 → 例外にせず null（必要ならログ出力に変えてもOK）
     */
    private Integer readInt(ResultSet rs, String column) throws SQLException {
        Object o = rs.getObject(column);
        if (o == null) return null;
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        if (o instanceof String) {
            String s = ((String) o).trim();
            if (s.isEmpty()) return null;
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException nfe) {
                // ログに出したければここで warn 出力に変更
                return null;
            }
        }
        return null;
    }
}