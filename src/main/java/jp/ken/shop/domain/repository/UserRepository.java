package jp.ken.shop.domain.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import jp.ken.shop.domain.entity.RegisterEntity;
import jp.ken.shop.domain.entity.UserEntity;
import jp.ken.shop.infrastructure.mapper.UserRowMapper;
@Repository
public class UserRepository {
    
    private final JdbcTemplate jdbc; 
    
    public UserRepository(JdbcTemplate jdbc) { 
        this.jdbc = jdbc;
    }

    // 会員IDで検索
    public List<UserEntity> findByMemberId(int memberId) {
        String sql = "SELECT * FROM t_member WHERE member_id = ?";
       
        return jdbc.query(sql, new UserRowMapper(), memberId);
    }
    
    // メールアドレスで検索
    public List<UserEntity> findByMemberEmail(String memberMail) {
        String sql = "SELECT * FROM t_member WHERE member_mail = ?";
       
        return jdbc.query(sql, new UserRowMapper(), memberMail);
    }
   
    public int insert(RegisterEntity entity) {

        String sql = "INSERT INTO t_member ("
                + "member_mail, member_name, member_kana, member_post, "
                + "member_address, member_phone, member_birthday, "
                + "member_payment, credit_type, credit_number, credit_name, credit_security_number, "
                + "member_password, member_point, signup_day, valid_flag"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getMemberMail());
            ps.setString(2, entity.getMemberName());
            ps.setString(3, entity.getMemberKana());
            ps.setString(4, entity.getMemberPost());
            ps.setString(5, entity.getMemberAddress());
            ps.setString(6, entity.getMemberPhone());
            ps.setObject(7, entity.getMemberBirthday());
            ps.setString(8, entity.getMemberPayment());
            ps.setString(9, entity.getCreditType());
            ps.setString(10, entity.getCreditNumber());
            ps.setString(11, entity.getCreditName());
            ps.setString(12, entity.getCreditSecurityNumber());
            ps.setString(13, entity.getMemberPassword());
            ps.setInt(14, entity.getMemberPoint());
            ps.setObject(15, entity.getSignupDay());
            ps.setString(16, entity.getValidFlag());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }



    

    
}
