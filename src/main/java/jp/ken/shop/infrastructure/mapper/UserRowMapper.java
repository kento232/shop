package jp.ken.shop.infrastructure.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import jp.ken.shop.domain.entity.UserEntity;

public class UserRowMapper implements RowMapper<UserEntity> {

    @Override
    public UserEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserEntity user = new UserEntity();

        user.setMemberId(rs.getInt("member_id"));
        user.setMemberMail(rs.getString("member_mail"));
        user.setMemberPassword(rs.getString("member_password"));

        return user;
    }
}
