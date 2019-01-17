package com.ajin.dao;

import com.ajin.domain.SysUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @Auther: ajin
 * @Date: 2019/1/17 13:15
 * @Description:
 */
@Repository
public interface UserMapper {
    SysUser findByUserName(@Param("username") String username);
}
