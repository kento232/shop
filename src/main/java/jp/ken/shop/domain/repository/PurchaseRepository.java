package jp.ken.shop.domain.repository;

import java.sql.Date;
import java.time.LocalDate;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PurchaseRepository {

    private final JdbcTemplate jdbc;

    public PurchaseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 次の purchase_id を採番（MAX+1）
     * ※ AUTO_INCREMENT じゃない設計のためアプリ側で採番
     *
     * 注意：同時購入が起きると重複する可能性がある。
     * 本番ならDBシーケンス/採番テーブル/UUID等が推奨。
     */
    public int nextPurchaseId() {
        String sql = "SELECT COALESCE(MAX(purchase_id), 0) + 1 FROM purchase";
        Integer next = jdbc.queryForObject(sql, Integer.class);
        return (next == null) ? 1 : next;
    }

    /**
     * purchase（1商品=1行）をINSERT
     * purchase_id_number は 1,2,3... を呼び出し側で渡す
     */
    public void insert(
            int purchaseId,
            int purchaseIdNumber,
            int memberId,
            LocalDate purchaseDay,
            String productId,
            int purchaseProductQuantity,
            String memberPayment,
            String designation,
            LocalDate sendScheduleDay,
            LocalDate sentDay
    ) {
        String sql = """
            INSERT INTO purchase (
              purchase_id,
              purchase_id_number,
              member_id,
              purchase_day,
              product_id,
              purchase_product_quantity,
              member_payment,
              designation,
              send_schedule_day,
              sent_day
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbc.update(
                sql,
                purchaseId,
                purchaseIdNumber,
                memberId,
                Date.valueOf(purchaseDay),
                productId,
                purchaseProductQuantity,
                memberPayment,
                designation,
                (sendScheduleDay == null ? null : Date.valueOf(sendScheduleDay)),
                (sentDay == null ? null : Date.valueOf(sentDay))
        );
    }

    /**
     * 注文をまとめて登録（カートの複数商品を一括INSERTしたい時用）
     * ※呼び出し側で @Transactional を付けて使うのが基本
     */
    @Transactional
    public int insertPurchaseLines(
            int memberId,
            LocalDate purchaseDay,
            java.util.List<PurchaseLine> lines,
            String memberPayment,
            String designation,
            LocalDate sendScheduleDay
    ) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("lines is empty");
        }

        int purchaseId = nextPurchaseId();

        int lineNo = 1;
        for (PurchaseLine line : lines) {
            insert(
                    purchaseId,
                    lineNo,
                    memberId,
                    purchaseDay,
                    line.productId(),
                    line.qty(),
                    memberPayment,
                    designation,
                    sendScheduleDay,
                    null
            );
            lineNo++;
        }
        return purchaseId;
    }

    /**
     * 存在チェック（必要なら）
     */
    public boolean exists(int purchaseId) {
        String sql = "SELECT COUNT(*) FROM purchase WHERE purchase_id = ?";
        Integer cnt = jdbc.queryForObject(sql, Integer.class, purchaseId);
        return cnt != null && cnt > 0;
    }

    /**
     * 発送済日に更新（必要なら）
     */
    public int markSent(int purchaseId, LocalDate sentDay) {
        String sql = "UPDATE purchase SET sent_day = ? WHERE purchase_id = ?";
        return jdbc.update(sql, Date.valueOf(sentDay), purchaseId);
    }

    /**
     * 1行分を取得（必要なら）
     */
    public PurchaseRow findOne(int purchaseId, int purchaseIdNumber) {
        String sql = """
            SELECT
              purchase_id,
              purchase_id_number,
              member_id,
              purchase_day,
              product_id,
              purchase_product_quantity,
              member_payment,
              designation,
              send_schedule_day,
              sent_day
            FROM purchase
            WHERE purchase_id = ?
              AND purchase_id_number = ?
            """;

        try {
            return jdbc.queryForObject(sql, (rs, rowNum) -> new PurchaseRow(
                    rs.getInt("purchase_id"),
                    rs.getInt("purchase_id_number"),
                    rs.getInt("member_id"),
                    rs.getDate("purchase_day").toLocalDate(),
                    rs.getString("product_id"),
                    rs.getInt("purchase_product_quantity"),
                    rs.getString("member_payment"),
                    rs.getString("designation"),
                    (rs.getDate("send_schedule_day") == null ? null : rs.getDate("send_schedule_day").toLocalDate()),
                    (rs.getDate("sent_day") == null ? null : rs.getDate("sent_day").toLocalDate())
            ), purchaseId, purchaseIdNumber);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // --- 小さいDTO（Java 21なら record が楽） ---

    /** 1商品分の入力（カート→購入行に変換する時用） */
    public record PurchaseLine(String productId, int qty) {}

    /** DB行の読み取り用 */
    public record PurchaseRow(
            int purchaseId,
            int purchaseIdNumber,
            int memberId,
            LocalDate purchaseDay,
            String productId,
            int purchaseProductQuantity,
            String memberPayment,
            String designation,
            LocalDate sendScheduleDay,
            LocalDate sentDay
    ) {}
}
