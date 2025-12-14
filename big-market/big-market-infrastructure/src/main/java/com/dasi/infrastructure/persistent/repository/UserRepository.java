package com.dasi.infrastructure.persistent.repository;

import com.dasi.domain.user.model.entity.UserEntity;
import com.dasi.domain.user.repository.IUserRepository;
import com.dasi.infrastructure.persistent.dao.IUserDao;
import com.dasi.infrastructure.persistent.po.User;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class UserRepository implements IUserRepository {

    @Resource
    private IUserDao userDao;

    @Override
    public UserEntity queryUserByUserId(String userId) {
        User user = userDao.queryUserByUserId(userId);
        if (user == null) return null;
        return UserEntity.builder()
                .userId(user.getUserId())
                .password(user.getPassword())
                .createTime(user.getCreateTime())
                .build();
    }

    @Override
    public void createUser(UserEntity userEntity) {
        User user = new User();
        user.setUserId(userEntity.getUserId());
        user.setPassword(userEntity.getPassword());
        userDao.insertUser(user);
    }
}
