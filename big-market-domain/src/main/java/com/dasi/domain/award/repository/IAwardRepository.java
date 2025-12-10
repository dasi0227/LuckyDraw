package com.dasi.domain.award.repository;

import com.dasi.domain.award.model.aggregate.DispatchHandleAggregate;
import com.dasi.domain.award.model.entity.*;

public interface IAwardRepository {

    void saveActivityAward(ActivityAwardEntity activityAwardEntity, TaskEntity taskEntity);

    AwardEntity queryAwardByAwardId(Long awardId);

    void createActivityAccountIfAbsent(String userId, Long activityId);

    void increaseActivityAccountPoint(DispatchHandleAggregate dispatchHandleAggregate);

    ActivityAccountEntity queryActivityAccount(String userId, Long activityId);

    ActivityAwardEntity queryActivityAwardByOrderId(String userId, String orderId);

    void saveUserAward(DispatchHandleAggregate dispatchHandleAggregate);
}