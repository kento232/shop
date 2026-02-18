package jp.ken.shop.domain.entity;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class LoginUser implements UserDetails {

    private final UserEntity user;

    public LoginUser(UserEntity user) {
        this.user = user;
    }

    // ★ 本名（member_name）を返す
    public String getRealName() {
        return user.getMemberName();
    }

    // ★ 会員IDを返す（必要なら）
    public Integer getMemberId() {
        return user.getMemberId();
    }

    // ★ メールアドレスを返す（必要なら）
    public String getEmail() {
        return user.getMemberMail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 権限が必要ならここに追加
    }

    @Override
    public String getPassword() {
        return user.getMemberPassword();
    }

    @Override
    public String getUsername() {
        return user.getMemberMail(); // ログインIDとしてメールを使用
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
