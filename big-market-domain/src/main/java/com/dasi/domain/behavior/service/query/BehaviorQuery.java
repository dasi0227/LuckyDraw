package com.dasi.domain.behavior.service.query;

import com.dasi.domain.behavior.repository.IBehaviorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class BehaviorQuery implements IBehaviorQuery {

    @Resource
    private IBehaviorRepository behaviorRepository;

    @Override
    public Boolean querySign(String userId, Long activityId) {
        Boolean isSign = behaviorRepository.querySign(userId, activityId);
        log.info("【查询】用户签到情况：userId={}, isSign={}", userId, isSign);
        return isSign;
    }

}
