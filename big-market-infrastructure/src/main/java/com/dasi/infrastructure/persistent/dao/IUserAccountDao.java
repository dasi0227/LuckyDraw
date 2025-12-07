package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.UserAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserAccountDao {

    UserAccount queryUserAccountByUserId(String userId);

    void createUserAccount(UserAccount userAccount);

    void decreaseUserAccountPoint(UserAccount userAccount);

    void increaseUserAccountPoint(UserAccount userAccount);

    Integer queryUserPointByUserId(String userId);
}
