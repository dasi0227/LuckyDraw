package com.dasi.domain.strategy.service.query;

import com.dasi.domain.strategy.model.io.ActivityAwardDetail;

import java.util.List;

public interface IStrategyQuery {

    List<ActivityAwardDetail> queryActivityAward(String userId, Long activityId);

}
