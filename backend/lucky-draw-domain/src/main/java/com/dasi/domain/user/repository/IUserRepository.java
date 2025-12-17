package com.dasi.domain.user.repository;

import com.dasi.domain.user.model.entity.UserEntity;

public interface IUserRepository {

    UserEntity queryUserByUserId(String userId);

    void createUser(UserEntity userEntity);

}
