package com.ajin.domain;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Auther: ajin
 * @Date: 2019/1/17 13:05
 * @Description:
 */
@Data
public class SysUser implements UserDetails {
    //UserDetails是Spring Security验证框架内部提供的用户验证接口，
    // 主要是来完成自定义用户认证功能，
    // 需要实现getAuthorities方法内容，将定义的角色列表添加到授权的列表内。
    private Integer id;
    private String username;
    private String password;
    private List<SysRole> roles;

    // 这是最重要的方法,必须重写
    //将定义的角色列表添加到授权的列表内。
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> auths = new ArrayList<>();
        List<SysRole> roles = getRoles();
        for(SysRole role:roles) {
            auths.add(new SimpleGrantedAuthority(role.getName()));
        }
        return auths;
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
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
