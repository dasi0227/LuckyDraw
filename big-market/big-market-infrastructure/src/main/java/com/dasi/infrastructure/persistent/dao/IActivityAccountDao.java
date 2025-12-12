package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.ActivityAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivityAccountDao {

    ActivityAccount queryActivityAccount(ActivityAccount activityAccount);

    Integer queryActivityAccountLuck(ActivityAccount activityAccount);

    Integer queryActivityAccountPoint(ActivityAccount activityAccount);

    void createActivityAccount(ActivityAccount activityAccount);

    void increaseActivityAccountRaffle(ActivityAccount activityAccount);

    int decreaseActivityAccountRaffle(ActivityAccount activityAccount);

    void increaseActivityAccountPoint(ActivityAccount activityAccount);

    void decreaseActivityAccountPoint(ActivityAccount activityAccount);

}
