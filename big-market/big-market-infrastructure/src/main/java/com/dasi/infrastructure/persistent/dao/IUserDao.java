package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserDao {

    User queryUserByUserId(String userId);

    void insertUser(User user);

}
