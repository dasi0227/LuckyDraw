package com.dasi.domain.activity.service.raffle.impl;

import com.dasi.domain.activity.model.dto.ActionChainCheck;
import com.dasi.domain.activity.model.dto.RaffleOrderAggregate;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.action.chain.ActionChainFactory;
import com.dasi.domain.activity.service.action.chain.IActionChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class DefaultActivityRaffle extends AbstractActivityRaffle {

    @Resource
    private ActionChainFactory actionChainFactory;

    public DefaultActivityRaffle(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Override
    protected RaffleOrderAggregate checkRaffleAvailable(String userId, ActivityEntity activityEntity) {

        ActionChainCheck actionChainCheck = new ActionChainCheck();
        actionChainCheck.setUserId(userId);
        actionChainCheck.setActivityId(activityEntity.getActivityId());
        actionChainCheck.setActivityEntity(activityEntity);

        IActionChain actionChain = actionChainFactory.getRaffleActionChain();
        return actionChain.action(actionChainCheck) ? actionChainCheck.getRaffleOrderAggregate() : null;
    }

}
