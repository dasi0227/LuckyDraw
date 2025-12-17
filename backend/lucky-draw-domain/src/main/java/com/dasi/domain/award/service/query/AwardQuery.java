package com.dasi.domain.award.service.query;

import com.dasi.domain.award.model.entity.UserAwardEntity;
import com.dasi.domain.award.model.io.QueryUserAwardContext;
import com.dasi.domain.award.model.io.QueryUserAwardResult;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AwardQuery implements IAwardQuery {

    @Resource
    private IAwardRepository awardRepository;

    @Override
    public List<QueryUserAwardResult> queryUserAwardRaffleList(QueryUserAwardContext queryAccountContext) {

        String userId = queryAccountContext.getUserId();
        Long activityId = queryAccountContext.getActivityId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        List<UserAwardEntity> userAwardEntityList = awardRepository.queryUserAwardRaffleList(userId, activityId);

        return userAwardEntityList.stream()
                .map(userAwardEntity -> QueryUserAwardResult.builder()
                        .awardId(userAwardEntity.getAwardId())
                        .awardName(userAwardEntity.getAwardName())
                        .awardTime(userAwardEntity.getAwardTime())
                        .build())
                .collect(Collectors.toList());
    }

}
