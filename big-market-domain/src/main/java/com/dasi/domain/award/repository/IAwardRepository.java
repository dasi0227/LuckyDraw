package com.dasi.domain.award.repository;

import com.dasi.domain.award.model.entity.ActivityAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.entity.AwardEntity;

public interface IAwardRepository {

    void saveActivityAward(ActivityAwardEntity activityAwardEntity, TaskEntity taskEntity);

    void updateActivityAwardState(ActivityAwardEntity activityAwardEntity);

    AwardEntity queryAwardByAwardId(Long awardId);

}
