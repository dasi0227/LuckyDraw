package com.dasi.domain.award.repository;

import com.dasi.domain.award.model.aggregate.AwardDispatchHandleAggregate;
import com.dasi.domain.award.model.entity.ActivityAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.entity.AwardEntity;

public interface IAwardRepository {

    void saveActivityAward(ActivityAwardEntity activityAwardEntity, TaskEntity taskEntity);

    AwardEntity queryAwardByAwardId(Long awardId);

    void createUserAccountIfAbsent(String userId);

    void increaseUserAccountPoint(AwardDispatchHandleAggregate awardDispatchHandleAggregate);

}