package jp.ken.shop.domain.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jp.ken.shop.domain.entity.UserEntity;
import jp.ken.shop.infrastructure.mapper.UserRowMapper;

@Repository
public class UserRepository {
    
    private final JdbcTemplate jdbc; 
    
    public UserRepository(JdbcTemplate jdbc) { 
        this.jdbc = jdbc;
    }

    // 会員IDで検索
    public List<UserEntity> findByMemberId(int id) {
        String sql = "SELECT * FROM t_member WHERE member_id = ?";
       
        return jdbc.query(sql, new UserRowMapper(), id);
    }
    
    // メールアドレスで検索
    public List<UserEntity> findByMemberEmail(String email) {
        String sql = "SELECT * FROM t_member WHERE member_mail = ?";
       
        return jdbc.query(sql, new UserRowMapper(), email);
    }
}
