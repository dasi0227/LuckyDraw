package com.dasi.domain.award.repository;

import com.dasi.domain.award.model.aggregate.DispatchHandleAggregate;
import com.dasi.domain.award.model.entity.*;

public interface IAwardRepository {

    void saveActivityAward(ActivityAwardEntity activityAwardEntity, TaskEntity taskEntity);

    AwardEntity queryAwardByAwardId(Long awardId);

    void createUserAccountIfAbsent(String userId);

    void increaseUserAccountPoint(DispatchHandleAggregate dispatchHandleAggregate);

    UserAccountEntity queryUserAccountByUserId(String userId);

    ActivityAwardEntity queryActivityAwardByOrderId(String userId, String orderId);

    void saveUserAward(DispatchHandleAggregate dispatchHandleAggregate);
}