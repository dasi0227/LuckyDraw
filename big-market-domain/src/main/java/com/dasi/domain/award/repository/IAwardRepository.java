package com.dasi.domain.award.repository;

import com.dasi.domain.award.model.entity.*;

import java.util.List;

public interface IAwardRepository {

    AwardEntity queryAwardByAwardId(Long awardId);

    ActivityAccountEntity queryActivityAccount(String userId, Long activityId);

    ActivityAwardEntity queryActivityAwardByOrderId(String userId, String orderId);

    List<UserAwardEntity> queryUserAwardRaffleList(String userId, Long activityId);

    void saveUserAward(ActivityAccountEntity activityAccountEntity, ActivityAwardEntity activityAwardEntity, UserAwardEntity userAwardEntity);

    void saveActivityAward(ActivityAwardEntity activityAwardEntity, TaskEntity taskEntity);

}