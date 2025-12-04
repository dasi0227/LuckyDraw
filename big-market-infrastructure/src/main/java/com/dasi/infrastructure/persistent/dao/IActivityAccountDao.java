package com.dasi.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.dasi.infrastructure.persistent.po.ActivityAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivityAccountDao {
    void rechargeActivityAccount(ActivityAccount activityAccount);

    void createActivityAccount(ActivityAccount activityAccount);

    int subtractActivityAccount(ActivityAccount activityAccount);

    @DBRouter
    ActivityAccount queryActivityAccount(ActivityAccount activityAccount);

}
