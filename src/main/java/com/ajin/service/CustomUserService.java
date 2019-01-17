package com.ajin.service;

import com.ajin.dao.UserMapper;
import com.ajin.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @Auther: ajin
 * @Date: 2019/1/17 17:44
 * @Description:
 */
@Service
public class CustomUserService implements UserDetailsService {
    @Autowired
    UserMapper userMapper;
    //重写loadUserByUsername方法，获得UserDetails类型用户
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user =userMapper.findByUserName(username);
        if(user!=null) {
            return  user;
        }else {
            throw new UsernameNotFoundException("user"+username+"doesn't exits!");
        }
    }
}
